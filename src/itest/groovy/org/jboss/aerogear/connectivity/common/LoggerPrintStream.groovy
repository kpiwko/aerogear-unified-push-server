/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.connectivity.common

import java.io.IOException;
import java.util.logging.Level
import java.util.logging.Logger

class LoggerPrintStream extends PrintStream {

    static final Logger log = Logger.getLogger(LoggerPrintStream.class.getName())

    Level loggingLevel
    StringBuffer buffer

    LoggerPrintStream() {
        this(Level.FINE)
    }

    LoggerPrintStream(Level loggingLevel) {
        super(new ByteArrayOutputStream())
        this.buffer = new StringBuffer()
        this.loggingLevel = loggingLevel
    }

    public void write(int b) {
        if( ((char)'\n') == ((char) b)){
            flush()
        }
        else {
            buffer.append((char)b)
        }
    }

    public void write(byte[] bytes) throws IOException {
        this.write(bytes, 0, bytes.length)
    }

    public void write(byte[] buf, int off, int len) {
        for(int i=0; i< len; i++) {
            this.write((int)buf[off+i])
        }
    }

    public void flush() {
        log.log(loggingLevel, buffer.toString())
        buffer.setLength(0)
    }
}
