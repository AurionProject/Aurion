/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services. 
 * All rights reserved. 
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met: 
 *     * Redistributions of source code must retain the above 
 *       copyright notice, this list of conditions and the following disclaimer. 
 *     * Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the documentation 
 *       and/or other materials provided with the distribution. 
 *     * Neither the name of the United States Government nor the 
 *       names of its contributors may be used to endorse or promote products 
 *       derived from this software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY 
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package gov.hhs.fha.nhinc.connectmgr.uddi.proxy;

/**
 * 
 * @author richard.ettema
 */
public class UDDIFindBusinessException extends Exception {

    private static final long serialVersionUID = -1776597002810532893L;

    /**
     * Default constructor.
     */
    public UDDIFindBusinessException() {
        super();
    }

    /**
     * Constructor with an envloping exception.
     * 
     * @param e The exception that caused this one.
     */
    public UDDIFindBusinessException(Exception e) {
        super(e);
    }

    /**
     * Constructor with the given exception and message.
     * 
     * @param sMessage The message to place in the exception.
     * @param e The exception that triggered this one.
     */
    public UDDIFindBusinessException(String sMessage, Exception e) {
        super(sMessage, e);
    }

    /**
     * Constructor with a given message.
     * 
     * @param sMessage The message for the exception.
     */
    public UDDIFindBusinessException(String sMessage) {
        super(sMessage);
    }
}
