/*
    MIT License
     
    Copyright (c) 2017 Michael Yan
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/
package org.nettymvc.data.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by myan on 12/15/2017.
 * Intellij IDEA
 */
final class ByteBufWriter extends Writer {
    
    private volatile boolean isOpen;
    private final ByteBuf byteBuf;
    private final Object lock;
    
    public ByteBuf getByteBuf() {
        return byteBuf;
    }
    
    ByteBufWriter() {
        this.lock = new Object();
        this.isOpen = true;
        this.byteBuf = ByteBufAllocator.DEFAULT.buffer();
    }
    
    @Override
    public void close() throws IOException {
        ensureOpen();
        synchronized (this.lock) {
            this.byteBuf.ensureWritable(byteBuf.writableBytes());
            this.isOpen = false;
        }
    }
    
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        synchronized (this.lock) {
            // append our byte to this buf.
            ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                    ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }
            for (char c : cbuf) {
                this.byteBuf.setByte(off, c);
                ++off;
            }
        }
    }
    
    @Override
    public void flush() {
    
    }
    
    private void ensureOpen() throws IOException {
        if (!this.isOpen) {
            throw new IOException("Stream closed!");
        }
    }
    
}
