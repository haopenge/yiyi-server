package com.peppa.common.security;

public enum Charset {
    ASCII("ascii"),
    ISO8859_1("iso8859-1"),
    GB2312("gb2312"),
    GBK("gbk"),
    GB18030("gb18030"),
    BIG5("big5"),
    UNICODE("unicode"),
    UTF8("utf-8"),
    UTF16("utf-16"),
    UTF32("utf-32");

    public final String VALUE;

    Charset(String VALUE) {
        this.VALUE = VALUE;
    }
}
