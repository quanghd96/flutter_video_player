package xyz.justsoft.video_thumbnail;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.view.TextureView;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.view.TextureRegistry;

public class VideoThumbnailPlugin implements EventChannel.StreamHandler {
    private static final String FORMAT_SS = "ss";
    private static final String FORMAT_DASH = "dash";
    private static final String FORMAT_HLS = "hls";
    private static final String FORMAT_OTHER = "other";

    private SimpleExoPlayer exoPlayer;
    private static Registrar mRegistrar;
    private EventChannel.EventSink eventSink;

    public static void registerWith(Registrar registrar) {
        mRegistrar = registrar;
        EventChannel eventChannel = new EventChannel(registrar.messenger(), "video_event");
        eventChannel.setStreamHandler(new VideoThumbnailPlugin());
    }

    @Override
    public void onListen(Object arguments, final EventChannel.EventSink events) {
        eventSink = events;
        Map<String, Object> args = (Map<String, Object>) arguments;
        String video = (String) args.get("video");
        playVideo(video);
    }

    @Override
    public void onCancel(Object arguments) {
        try {
            exoPlayer.stop();
            exoPlayer.release();
        } catch (Exception e) {
        }
    }

    private void playVideo(String url) {
        exoPlayer = new SimpleExoPlayer.Builder(mRegistrar.context()).build();
        Uri uri = Uri.parse(url);
        DataSource.Factory dataSourceFactory =
                new DefaultHttpDataSourceFactory(
                        "ExoPlayer",
                        null,
                        DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                        DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                        true
                );
        MediaSource mediaSource = buildMediaSource(uri, dataSourceFactory, null, mRegistrar.context());
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
        setupVideoPlayer();
        exoPlayer.setPlayWhenReady(true);
    }

    private MediaSource buildMediaSource(
            Uri uri, DataSource.Factory mediaDataSourceFactory, String formatHint, Context context) {
        int type;
        if (formatHint == null) {
            type = Util.inferContentType(uri.getLastPathSegment());
        } else {
            switch (formatHint) {
                case FORMAT_SS:
                    type = C.TYPE_SS;
                    break;
                case FORMAT_DASH:
                    type = C.TYPE_DASH;
                    break;
                case FORMAT_HLS:
                    type = C.TYPE_HLS;
                    break;
                case FORMAT_OTHER:
                    type = C.TYPE_OTHER;
                    break;
                default:
                    type = -1;
                    break;
            }
        }
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        new DefaultDataSourceFactory(context, null, mediaDataSourceFactory))
                        .createMediaSource(MediaItem.fromUri(uri));
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        new DefaultDataSourceFactory(context, null, mediaDataSourceFactory))
                        .createMediaSource(MediaItem.fromUri(uri));
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(uri));
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(mediaDataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(uri));
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private void setupVideoPlayer() {
        TextureRegistry.SurfaceTextureEntry handle =
                mRegistrar.textures().createSurfaceTexture();
        TextureView textureView = new TextureView(mRegistrar.context());
        textureView.setAlpha(0);
        textureView.setSurfaceTexture(handle.surfaceTexture());
        exoPlayer.setVideoTextureView(textureView);

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, width);
        mRegistrar.activity().addContentView(textureView, params);

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
                try {
                    Bitmap bmp = textureView.getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    bmp.recycle();
                    eventSink.success(byteArray);
                } catch (Exception e) {
                }
            }
        });
    }
}
