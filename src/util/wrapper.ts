import type { Frame } from 'react-native-vision-camera';
import type { MRZFrame } from '../types/types';

export default function scanMRZ(frame: Frame): MRZFrame {
  'worklet';

  // --- AÑADE ESTOS LOGS ---
  // Estos logs se ejecutarán en el contexto del Worklet (Hermes)
  console.log('Worklet: Attempting to call __scanMRZ');
  // Verifica si la propiedad existe en el objeto global de Hermes
  if (typeof (global as any).__scanMRZ === 'function') {
    console.log('Worklet: __scanMRZ IS available on global object!');
  } else {
    console.warn('Worklet: __scanMRZ IS NOT available on global object. Type:', typeof (global as any).__scanMRZ);
    // Puedes incluso intentar enumerar propiedades cercanas para depurar
    // console.warn('Worklet: Global object keys:', Object.keys(global as any));
  }
  // -------------------------

  // @ts-ignore
  return __scanMRZ(frame); // Esta es la línea que falla
}
