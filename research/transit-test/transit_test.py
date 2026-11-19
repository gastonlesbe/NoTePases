#!/usr/bin/env python3
"""Compare public-transport routing from Google Routes API and Transitous (MOTIS)
for a fixed list of trips between public places. Writes results.json and results.md.

Google key: read from the file in GOOGLE_KEY_FILE (default ~/.config/gpsalert/routes_key).
The key is never printed. If the file is missing, only Transitous runs.
"""
import datetime as dt
import json
import os
import sys
import time
import urllib.parse
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
UA = "transit-routing-test/1.0"
KEY_FILE = os.environ.get("GOOGLE_KEY_FILE", os.path.expanduser("~/.config/gpsalert/routes_key"))

# Public places only (stations, monuments, universities). Coordinates are approximate.
TRIPS = [
    ("BA", "Obelisco", (-34.60374, -58.38159), "Estación Tigre", (-34.42283, -58.57910)),
    ("BA", "Plaza Constitución", (-34.62786, -58.38146), "Estación La Plata", (-34.90867, -57.95039)),
    ("BA", "Estación Liniers", (-34.64246, -58.52086), "Plaza Italia", (-34.58095, -58.42102)),
    ("BA", "Retiro", (-34.59130, -58.37470), "Aeroparque Jorge Newbery", (-34.55810, -58.41640)),
    ("MdP", "Terminal de Ómnibus Mar del Plata", (-38.01510, -57.56360), "Puerto de Mar del Plata", (-38.04330, -57.53450)),
    ("MdP", "Plaza San Martín (MdP)", (-38.00470, -57.54900), "Complejo Universitario UNMdP", (-38.01280, -57.58050)),
    ("CBA", "Terminal de Córdoba", (-31.42400, -64.17490), "Ciudad Universitaria UNC", (-31.43790, -64.18580)),
    ("ROS", "Monumento a la Bandera", (-32.94760, -60.63060), "Terminal Mariano Moreno", (-32.94380, -60.66760)),
    ("SP", "MASP (Av. Paulista)", (-23.56140, -46.65590), "Rodoviária Tietê", (-23.51630, -46.62530)),
    ("RJ", "Copacabana (Posto 5)", (-22.97670, -43.19000), "Maracanã", (-22.91220, -43.23020)),
]

# Next weekday 08:30 local (UTC-3 for all trips above).
def departure_utc():
    d = dt.date.today() + dt.timedelta(days=1)
    while d.weekday() >= 5:
        d += dt.timedelta(days=1)
    return dt.datetime(d.year, d.month, d.day, 11, 30, tzinfo=dt.timezone.utc)


def http_json(url, data=None, headers=None, timeout=40):
    req = urllib.request.Request(url, data=data, headers={"User-Agent": UA, **(headers or {})})
    with urllib.request.urlopen(req, timeout=timeout) as r:
        return json.load(r)


def transitous(o, d, when):
    q = urllib.parse.urlencode({
        "fromPlace": f"{o[0]},{o[1]}", "toPlace": f"{d[0]},{d[1]}",
        "time": when.strftime("%Y-%m-%dT%H:%M:%SZ"), "numItineraries": 3,
    })
    res = http_json("https://api.transitous.org/api/v5/plan?" + q)
    out = []
    for it in res.get("itineraries", [])[:3]:
        legs = [l for l in it["legs"] if l["mode"] not in ("WALK", "BIKE", "CAR")]
        if not legs:
            continue
        out.append({
            "minutes": round(it["duration"] / 60),
            "legs": [{
                "mode": l["mode"], "line": l.get("routeShortName") or l.get("routeLongName") or "",
                "board": l["from"].get("name"), "alight": l["to"].get("name"),
                "alight_latlng": [l["to"].get("lat"), l["to"].get("lon")],
                "stops": len(l.get("intermediateStops") or []) + 1 if l.get("intermediateStops") is not None else None,
            } for l in legs],
        })
    return out


def google(o, d, when, key):
    body = {
        "origin": {"location": {"latLng": {"latitude": o[0], "longitude": o[1]}}},
        "destination": {"location": {"latLng": {"latitude": d[0], "longitude": d[1]}}},
        "travelMode": "TRANSIT",
        "computeAlternativeRoutes": True,
        "departureTime": when.strftime("%Y-%m-%dT%H:%M:%SZ"),
        "languageCode": "es-419",
        "transitPreferences": {"allowedTravelModes": ["BUS", "SUBWAY", "TRAIN", "LIGHT_RAIL", "RAIL"]},
    }
    headers = {
        "Content-Type": "application/json",
        "X-Goog-Api-Key": key,
        "X-Goog-FieldMask": "routes.duration,routes.legs.steps.travelMode,routes.legs.steps.transitDetails",
    }
    res = http_json("https://routes.googleapis.com/directions/v2:computeRoutes",
                    data=json.dumps(body).encode(), headers=headers)
    out = []
    for r in res.get("routes", [])[:3]:
        legs = []
        for leg in r.get("legs", []):
            for s in leg.get("steps", []):
                td = s.get("transitDetails")
                if s.get("travelMode") != "TRANSIT" or not td:
                    continue
                line = td.get("transitLine", {})
                arr = td.get("stopDetails", {}).get("arrivalStop", {})
                legs.append({
                    "mode": line.get("vehicle", {}).get("type", ""),
                    "line": line.get("nameShort") or line.get("name") or "",
                    "board": td.get("stopDetails", {}).get("departureStop", {}).get("name"),
                    "alight": arr.get("name"),
                    "alight_latlng": [arr.get("location", {}).get("latLng", {}).get("latitude"),
                                      arr.get("location", {}).get("latLng", {}).get("longitude")],
                    "stops": td.get("stopCount"),
                })
        if legs:
            out.append({"minutes": round(int(r.get("duration", "0s").rstrip("s")) / 60), "legs": legs})
    return out


def fmt(opts):
    if opts is None:
        return "_not run_"
    if isinstance(opts, str):
        return f"_error: {opts}_"
    if not opts:
        return "**no route**"
    lines = []
    for k, o in enumerate(opts, 1):
        parts = [f"{l['mode']} {l['line']}: {l['board']} → **{l['alight']}**" + (f" ({l['stops']} paradas)" if l['stops'] else "")
                 for l in o["legs"]]
        lines.append(f"{k}. {o['minutes']} min — " + " · ".join(parts))
    return "<br>".join(lines)


def main():
    key = None
    if os.path.exists(KEY_FILE):
        key = open(KEY_FILE).read().strip() or None
    when = departure_utc()
    results = []
    for region, on, o, dn, d in TRIPS:
        row = {"region": region, "from": on, "to": dn}
        for name, fn in (("transitous", lambda: transitous(o, d, when)),
                         ("google", (lambda: google(o, d, when, key)) if key else None)):
            if fn is None:
                row[name] = None
                continue
            try:
                row[name] = fn()
            except Exception as e:  # keep going; record the error without secrets
                row[name] = f"{type(e).__name__}: {str(e)[:120]}"
            time.sleep(1)
        results.append(row)
        print(region, on, "→", dn, "| transitous:", "n/a" if not isinstance(row["transitous"], list) else len(row["transitous"]),
              "| google:", "not run" if row["google"] is None else (len(row["google"]) if isinstance(row["google"], list) else "error"),
              file=sys.stderr)
    json.dump({"departure_utc": when.isoformat(), "results": results}, open(os.path.join(HERE, "results.json"), "w"),
              ensure_ascii=False, indent=1)
    md = [f"# Transit routing test — departure {when.astimezone(dt.timezone(dt.timedelta(hours=-3))):%a %d/%m %H:%M} (UTC-3)", "",
          "| Zona | Viaje | Google Routes | Transitous |", "|---|---|---|---|"]
    for r in results:
        md.append(f"| {r['region']} | {r['from']} → {r['to']} | {fmt(r['google'])} | {fmt(r['transitous'])} |")
    open(os.path.join(HERE, "results.md"), "w").write("\n".join(md) + "\n")
    print("wrote results.md", file=sys.stderr)


if __name__ == "__main__":
    main()
