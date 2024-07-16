package com.printlibrary.textparser;

import com.printlibrary.EscPosPrinterCommands;
import com.printlibrary.exceptions.EscPosConnectionException;
import com.printlibrary.exceptions.EscPosEncodingException;

public interface IPrinterTextParserElement {
    int length() throws EscPosEncodingException;
    IPrinterTextParserElement print(EscPosPrinterCommands printerSocket) throws EscPosEncodingException, EscPosConnectionException;
}
