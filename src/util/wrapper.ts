import type { Frame } from 'react-native-vision-camera';
import type { MRZFrame } from '../types/types'; // Asegúrate de que esta ruta sea correcta

export default function scanMRZ(frame: Frame): MRZFrame | null {
  'worklet';

  console.log('Worklet: Attempting to call __scanMRZ');
  if (typeof (global as any).__scanMRZ === 'function') {
    console.log('Worklet: __scanMRZ IS available on global object!');
    try {
      // @ts-ignore
      return __scanMRZ(frame);
    } catch (e: any) {
      console.error('Worklet: Error during __scanMRZ execution:', e.message);
      return null;
    }
  } else {
    console.warn('Worklet: __scanMRZ IS NOT available on global object. Type:', typeof (global as any).__scanMRZ);
    return null;
  }
}
