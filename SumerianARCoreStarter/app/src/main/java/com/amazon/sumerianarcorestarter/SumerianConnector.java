// Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
// http://aws.amazon.com/apache2.0/
// or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
//
package com.amazon.sumerianarcorestarter;

import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

class SumerianConnector {

    private WebView mWebView;
    private Session mSession;
    private GLSurfaceView mSurfaceView;

    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];

    SumerianConnector(WebView webView, Session session, GLSurfaceView surfaceView) {
        mWebView = webView;
        mSession = session;
        mSurfaceView = surfaceView;

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new BridgeInterface(), "Android");

        this.mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.setBackgroundColor(0x00000000);
            }
        });
    }

    void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    void update() {
        final Frame frame = mSession.update();
        final Camera camera = frame.getCamera();

        if (camera.getTrackingState() == TrackingState.PAUSED) {
            return;
        }

        camera.getViewMatrix(mViewMatrix, 0);
        camera.getProjectionMatrix(mProjectionMatrix, 0, 0.02f, 20.0f);

        final String cameraUpdateString = "ARCoreBridge.viewProjectionMatrixUpdate('" + serializeArray(mViewMatrix) +"', '"+ serializeArray(mProjectionMatrix) + "');";
        evaluateWebViewJavascript(cameraUpdateString);

        HashMap<String, float[]> anchorMap = new HashMap<>();

        for (Anchor anchor : mSession.getAllAnchors()) {
            if (anchor.getTrackingState() != TrackingState.TRACKING) {
                continue;
            }

            final float[] anchorPoseMatrix = new float[16];
            anchor.getPose().toMatrix(anchorPoseMatrix, 0);
            anchorMap.put(String.valueOf(anchor.hashCode()), anchorPoseMatrix);
        }

        if (anchorMap.size() > 0) {
            JSONObject jsonAnchors = new JSONObject(anchorMap);
            final String anchorUpdateScript = "ARCoreBridge.anchorTransformUpdate('" + jsonAnchors.toString() + "');";
            evaluateWebViewJavascript(anchorUpdateScript);
        }

        if (frame.getLightEstimate().getState() != LightEstimate.State.NOT_VALID) {
            final String lightEstimateUpdateScript = "ARCoreBridge.lightingEstimateUpdate(" + String.valueOf(frame.getLightEstimate().getPixelIntensity()) + ");";
            evaluateWebViewJavascript(lightEstimateUpdateScript);
        }
    }

    private String serializeArray(float[] array) {
        try {
            JSONArray jsonArray = new JSONArray(array);
            return jsonArray.toString();
        } catch (JSONException e) {
            return "";
        }
    }

    private void evaluateWebViewJavascript(final String scriptString) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final Runnable webViewUpdate = new Runnable() {
            @Override
            public void run() {
                mWebView.evaluateJavascript(scriptString, null);
            }
        };

        mainHandler.postAtFrontOfQueue(webViewUpdate);
    }

    private class BridgeInterface {

        private float[] mHitTestResultPose = new float[16];

        @JavascriptInterface
        public void requestHitTest(final String requestId, final float screenX, final float screenY) {
            if (requestId == null) {
                return;
            }

            mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    final float hitTestX = screenX * mWebView.getWidth();
                    final float hitTestY = screenY * mWebView.getHeight();

                    List<HitResult> hitTestResults = mSession.update().hitTest(hitTestX, hitTestY);

                    final String scriptString;

                    if (hitTestResults.size() > 0) {
                        hitTestResults.get(0).getHitPose().toMatrix(mHitTestResultPose, 0);
                        scriptString = "ARCoreBridge.hitTestResponse('" + requestId + "', '" + serializeArray(mHitTestResultPose) + "');";

                    } else {
                        scriptString = "ARCoreBridge.hitTestResponse('" + requestId + "', null);";
                    }

                    evaluateWebViewJavascript(scriptString);
                }
            });
        }

        @JavascriptInterface
        public void registerAnchor(final String requestId, final float[] matrix) {
            if (requestId == null || matrix == null) {
                return;
            }

            mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    Pose anchorPose = Pose.makeTranslation(matrix[12], matrix[13], matrix[14]);
                    Anchor anchor = mSession.createAnchor(anchorPose);

                    final String scriptString = "ARCoreBridge.registerAnchorResponse('" + requestId + "', '" + String.valueOf(anchor.hashCode()) + "');";
                    evaluateWebViewJavascript(scriptString);
                }
            });
        }
    }
}
