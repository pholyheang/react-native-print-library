package com.printlibrary;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.module.annotations.ReactModule;
import com.printlibrary.connection.DeviceConnection;
import com.printlibrary.connection.usb.UsbConnection;
import com.printlibrary.connection.usb.UsbPrintersConnections;
import com.printlibrary.async.AsyncEscPosPrint;
import com.printlibrary.async.AsyncEscPosPrinter;
import com.printlibrary.async.AsyncUsbEscPosPrint;
import com.printlibrary.textparser.PrinterTextParserImg;

@ReactModule(name = PrintLibraryModule.NAME)
public class PrintLibraryModule extends ReactContextBaseJavaModule {
  public static final String NAME = "PrintLibrary";

  private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
  private final ReactApplicationContext reactContext;
  private final  UsbManager usbManager;
  private final  UsbConnection usbConnection;
  public PrintLibraryModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
     usbConnection = UsbPrintersConnections.selectFirstConnected(reactContext);
     usbManager = (UsbManager) reactContext.getSystemService(Context.USB_SERVICE);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }


  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  public void multiply(double a, double b, Promise promise) {
    promise.resolve(a * b+9);
  }

  @ReactMethod
  public void  printImage64(String img64, Promise promise) {

    if (usbConnection == null || usbManager == null) {
      promise.resolve("no printer");
      return;
    }

    final Activity activity = getCurrentActivity();
    if (activity == null || activity.isFinishing()) {
      promise.resolve("Activity is not running");
      return;
    }

    PendingIntent permissionIntent = PendingIntent.getBroadcast(
      reactContext,
      0,
      new Intent(ACTION_USB_PERMISSION),
      android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE : 0);

    BroadcastReceiver usbReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_USB_PERMISSION.equals(action)) {
          synchronized (this) {
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
              if (usbManager != null && usbDevice != null) {
                try {
                  new AsyncUsbEscPosPrint(
                    context,
                    new AsyncEscPosPrint.OnPrintFinished() {
                      @Override
                      public void onError(AsyncEscPosPrinter asyncEscPosPrinter, int codeException) {
                        promise.resolve("An error occurred !");
                        Log.e("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : An error occurred !");

                      }

                      @Override
                      public void onSuccess(AsyncEscPosPrinter asyncEscPosPrinter) {
                        promise.resolve("Print is finished !");
                        Log.i("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : Print is finished !");
                      }
                    }).execute(getAsyncEscPosPrinterImg64(new UsbConnection(usbManager, usbDevice),img64));

                } catch (Exception exception){
                  promise.resolve("exception " + exception);
                }
              }
            }
          }
        }
      }
    };

    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    reactContext.registerReceiver(usbReceiver, filter);
    usbManager.requestPermission(usbConnection.getDevice(), permissionIntent);
  }

  @ReactMethod
  public void printText(String a, Promise promise) {

    if (usbConnection == null || usbManager == null) {
      promise.resolve("no printer");
      return;
    }

    final Activity activity = getCurrentActivity();
    if (activity == null || activity.isFinishing()) {
      promise.resolve("Activity is not running");
      return;
    }



    PendingIntent permissionIntent = PendingIntent.getBroadcast(
      reactContext,
      0,
      new Intent(ACTION_USB_PERMISSION),
      android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE : 0);

    BroadcastReceiver usbReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_USB_PERMISSION.equals(action)) {
          synchronized (this) {
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
              if (usbManager != null && usbDevice != null) {
                try {
                  new AsyncUsbEscPosPrint(
                    context,
                    new AsyncEscPosPrint.OnPrintFinished() {
                      @Override
                      public void onError(AsyncEscPosPrinter asyncEscPosPrinter, int codeException) {
                          promise.resolve("An error occurred !");
                        Log.e("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : An error occurred !");

                      }

                      @Override
                      public void onSuccess(AsyncEscPosPrinter asyncEscPosPrinter) {
                        promise.resolve("Print is finished !");
                        Log.i("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : Print is finished !");
                      }
                    }).execute(getAsyncEscPosPrinter(new UsbConnection(usbManager, usbDevice)));

                } catch (Exception exception){
                  promise.resolve("exception " + exception);
                }
              }
            }
          }
        }
      }
    };

    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    reactContext.registerReceiver(usbReceiver, filter);
    usbManager.requestPermission(usbConnection.getDevice(), permissionIntent);

  }

  public static Bitmap convert(String base64Str) throws IllegalArgumentException
  {
    byte[] decodedBytes = Base64.decode(
      base64Str.substring(base64Str.indexOf(",")  + 1),
      Base64.DEFAULT
    );
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
  }

  public AsyncEscPosPrinter getAsyncEscPosPrinterImg64(DeviceConnection printerConnection,String img64) {
    Bitmap drawingCache = convert(img64);
    AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 80, 32);
    // Assuming you have a method to add bitmap image to the printer
    String escPosImage = PrinterTextParserImg.bitmapToHexadecimalString(printer, drawingCache);

    // Use the addTextToPrint method to add the image to the print queue
    return printer.addTextToPrint("[L]<img>" + escPosImage + "</img>\n");
  }

  public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
    AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 80, 32);
    return printer.addTextToPrint(
      "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
        "[L]\n" +
        "[C]<u type='double'> date </u>\n" +
        "[C]\n" +
        "[C]================================\n" +
        "[L]\n" +
        "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99€\n" +
        "[L]  + Size : S\n" +
        "[L]\n" +
        "[L]<b>AWESOME HAT</b>[R]24.99€\n" +
        "[L]  + Size : 57/58\n" +
        "[L]\n" +
        "[C]--------------------------------\n" +
        "[R]TOTAL PRICE :[R]34.98€\n" +
        "[R]TAX :[R]4.23€\n" +
        "[L]\n" +
        "[C]================================\n" +
        "[L]\n" +
        "[L]<u><font color='bg-black' size='tall'>Customer :</font></u>\n" +
        "[L]Raymond DUPONT\n" +
        "[L]5 rue des girafes\n" +
        "[L]31547 PERPETES\n" +
        "[L]Tel : +33801201456\n" +
        "\n" +
        "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
        "[L]\n" +
        "[C]<qrcode size='20'>https://iprint.com/</qrcode>\n");
  }
}
