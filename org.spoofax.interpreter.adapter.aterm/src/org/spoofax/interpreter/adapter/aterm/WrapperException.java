/*
 * Created on 23. sep.. 2006
 *
 * Copyright (c) 2005, Karl Trygve Kalleberg <karltk near strategoxt.org>
 * 
 * Licensed under the GNU General Public License, v2
 */
package org.spoofax.interpreter.adapter.aterm;

public class WrapperException extends UnsupportedOperationException {

    private String message;
    
    public WrapperException(String message) {
        this.message = message;
    }

    public WrapperException() {
        this.message = "";
    }
    
    private static final long serialVersionUID = 5842627880573294319L;

    @Override
    public String toString() {
        return getClass() + ":" + message;
    }
}