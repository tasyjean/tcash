package com.skcc.wallet.framework.api.http.model;

public class GetUserProfileRq {
    private CommonHeader commonHeader;

    public CommonHeader getCommonHeader() {
        return this.commonHeader;
    }

    public void setCommonHeader(CommonHeader commonHeader) {
        this.commonHeader = commonHeader;
    }
}
