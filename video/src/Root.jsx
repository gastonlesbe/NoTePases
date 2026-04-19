import { Composition } from 'remotion';
import { NoTePasesVideo } from './NoTePasesVideo';

export const RemotionRoot = () => {
  return (
    <>
      <Composition
        id="GPSAlertES"
        component={NoTePasesVideo}
        defaultProps={{ lang: 'es' }}
        durationInFrames={60 * 25}
        fps={60}
        width={1920}
        height={1080}
      />
      <Composition
        id="GPSAlertEN"
        component={NoTePasesVideo}
        defaultProps={{ lang: 'en' }}
        durationInFrames={60 * 25}
        fps={60}
        width={1920}
        height={1080}
      />
    </>
  );
};
