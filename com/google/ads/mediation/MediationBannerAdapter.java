package com.google.ads.mediation;

import android.app.Activity;
import android.view.View;
import com.google.ads.C0632b;

@Deprecated
public interface MediationBannerAdapter<ADDITIONAL_PARAMETERS extends NetworkExtras, SERVER_PARAMETERS extends MediationServerParameters> extends MediationAdapter<ADDITIONAL_PARAMETERS, SERVER_PARAMETERS> {
    View getBannerView();

    void requestBannerAd(MediationBannerListener mediationBannerListener, Activity activity, SERVER_PARAMETERS server_parameters, C0632b c0632b, MediationAdRequest mediationAdRequest, ADDITIONAL_PARAMETERS additional_parameters);
}
