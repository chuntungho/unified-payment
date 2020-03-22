package com.chuntung.payment.dto;

import java.io.Serializable;

public class FormResult<T> implements Serializable{
    private static final long serialVersionUID = -1099078272257614327L;

    private String formUrl;
    private T formParam;

    public FormResult(String formUrl, T formParam) {
        this.formUrl = formUrl;
        this.formParam = formParam;
    }

    public String getFormUrl() {
        return formUrl;
    }

    public void setFormUrl(String formUrl) {
        this.formUrl = formUrl;
    }

    public T getFormParam() {
        return formParam;
    }

    public void setFormParam(T formParam) {
        this.formParam = formParam;
    }
}
