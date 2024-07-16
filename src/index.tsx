import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-print-library' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const PrintLibrary = NativeModules.PrintLibrary
  ? NativeModules.PrintLibrary
  : new Proxy(
    {},
    {
      get() {
        throw new Error(LINKING_ERROR);
      },
    }
  );

export function multiply(a: number, b: number): Promise<number> {
  return PrintLibrary.multiply(a, b);
}


export function printText(a: string,): Promise<string> {
  return PrintLibrary.printText(a);
}

export function printImage64(img64: string): Promise<string> {
  return PrintLibrary.printImage64(img64);
}
