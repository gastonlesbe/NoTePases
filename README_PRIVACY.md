# Configuración de Política de Privacidad en GitHub Pages

Este documento explica cómo configurar la política de privacidad de la app "No Te Pases" en GitHub Pages para cumplir con los requisitos de Google Play Store.

## Pasos para Configurar GitHub Pages

### 1. Crear un Repositorio en GitHub

1. Ve a [GitHub](https://github.com) y crea un nuevo repositorio (por ejemplo: `notepases-privacy` o `notepases-policy`)
2. O si ya tienes un repositorio para el proyecto, puedes crear una rama `gh-pages` o usar la carpeta `docs/`

### 2. Subir el Archivo HTML

**Opción A: Repositorio Separado (Recomendado)**
- Crea un repositorio nuevo solo para la política de privacidad
- Sube el archivo `privacy-policy.html` a la raíz del repositorio
- Renómbralo a `index.html` para que sea la página principal

**Opción B: Usar el Repositorio del Proyecto**
- Crea una carpeta `docs/` en la raíz del proyecto
- Coloca `privacy-policy.html` dentro de `docs/` y renómbralo a `index.html`
- O crea una rama `gh-pages` y coloca el archivo allí

### 3. Habilitar GitHub Pages

1. Ve a la configuración del repositorio en GitHub
2. Navega a **Settings** → **Pages**
3. En **Source**, selecciona:
   - Si usas un repositorio separado: **Deploy from a branch** → **main** → **/ (root)**
   - Si usas la carpeta `docs/`: **Deploy from a branch** → **main** → **/docs**
4. Guarda los cambios

### 4. Obtener la URL

Después de unos minutos, tu política de privacidad estará disponible en:
- Si el repositorio se llama `notepases-privacy` y tu usuario es `tuusuario`:
  ```
  https://tuusuario.github.io/notepases-privacy/
  ```

### 5. Agregar la URL en Google Play Console

1. Ve a [Google Play Console](https://play.google.com/console)
2. Selecciona tu app "No Te Pases"
3. Ve a **Política → Política de privacidad**
4. Ingresa la URL de tu GitHub Pages (ejemplo: `https://tuusuario.github.io/notepases-privacy/`)
5. Guarda los cambios

## Estructura Recomendada

```
repositorio-privacy/
├── index.html (redirige a privacy-policy.html)
├── privacy-policy.html (versión en español)
├── privacy-policy-en.html (versión en inglés)
├── privacy-policy-pt.html (versión en portugués)
└── README.md (opcional)
```

**Nota:** Si prefieres que la versión en español sea la página principal, puedes renombrar `privacy-policy.html` a `index.html` y eliminar el archivo `index.html` de redirección.

## Verificación

Después de configurar GitHub Pages, verifica que:
- ✅ La página carga correctamente
- ✅ El diseño se ve bien en móvil y desktop
- ✅ Todos los enlaces funcionan
- ✅ La fecha de última actualización es correcta
- ✅ La información de contacto es correcta

## Actualizar la Política

Cuando necesites actualizar la política:
1. Edita el archivo `privacy-policy.html` localmente
2. Actualiza la fecha de "Última actualización"
3. Sube los cambios a GitHub
4. Los cambios se reflejarán automáticamente en GitHub Pages

## Notas Importantes

- GitHub Pages es gratuito y adecuado para políticas de privacidad estáticas
- La URL será permanente mientras el repositorio exista
- Puedes personalizar el dominio si tienes uno propio
- Asegúrate de que la política esté siempre accesible (no elimines el repositorio)

## Alternativas

Si prefieres no usar GitHub Pages, puedes:
- Usar tu propio sitio web
- Usar servicios como Netlify, Vercel, o Firebase Hosting
- Usar Google Sites (gratis)

Cualquier servicio que proporcione una URL HTTPS pública funcionará para Google Play Store.

