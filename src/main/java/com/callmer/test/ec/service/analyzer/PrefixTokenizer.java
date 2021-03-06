/**
 * $Id$
 * Copyright 2009-2012 Oak Pacific Interactive. All rights reserved.
 */
package com.callmer.test.ec.service.analyzer;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeSource;


/**
 *  前缀匹配的tokenizer
* @author <a href="mailto:rebricate@gmail.com">刘刚</a>
 * @version 2013-9-28
 */

public final class PrefixTokenizer extends Tokenizer {


    public PrefixTokenizer(Reader in) {
      super(in);
    }

    public PrefixTokenizer(AttributeSource source, Reader in) {
      super(source, in);
    }

    public PrefixTokenizer(AttributeFactory factory, Reader in) {
      super(factory, in);
    }
       
    private int offset = 0, bufferIndex=0, dataLen=0;
    private final static int MAX_WORD_LEN = 255;
    private final static int IO_BUFFER_SIZE = 1024;
    private final char[] ioBuffer = new char[IO_BUFFER_SIZE];


    private int length;
    private int start;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    
    //写入字元和字元位置写入字典
    private final boolean flush() {
        if (length>0  && length <= MAX_WORD_LEN) {
          termAtt.copyBuffer(ioBuffer, 0, start +length);
          offsetAtt.setOffset(correctOffset(start), correctOffset(start+length));
          return true;
        }
        else
            return false;
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();

        length = 0;
        start = offset;

        while (true) {
            offset++;
            //读取一次，第二次在读取就是返回-1
            if (bufferIndex >= dataLen) {
                dataLen = input.read(ioBuffer);
                bufferIndex = 0;
            }

            if (dataLen == -1) {
              offset--;
              return flush();
            } else
            {
                bufferIndex++;
            }
            if (length == 0) {
                start = offset-1;            // start of token
            }
            length++;
            return flush();
        }
    }
    
    @Override
    public final void end() {
      // set final offset
      final int finalOffset = correctOffset(offset);
      this.offsetAtt.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void reset() throws IOException {
      super.reset();
      offset = bufferIndex = dataLen = 0;
    }
}

