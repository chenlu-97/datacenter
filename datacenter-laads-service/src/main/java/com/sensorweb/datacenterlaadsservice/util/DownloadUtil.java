package com.sensorweb.datacenterlaadsservice.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件下载工具类（单例模式）
 */
@Component
@Slf4j
public class DownloadUtil {

    @Autowired
    private OkHttpClient okHttpClient;



    public interface OnDownloadListener{

        /**
         * 下载成功之后的文件
         */
        void onDownloadSuccess(File file) throws Exception;

        /**
         * 下载进度
         */
        void onDownloading(int progress);

        /**
         * 下载异常信息
         */

        void onDownloadFailed(Exception e);
    }

    /**
     * @param url          下载连接
     * @param destFileDir  下载的文件储存目录
     * @param destFileName 下载文件名称，后面记得拼接后缀，否则手机没法识别文件类型
     * @param listener     下载监听
     */
    public void downloadBySink(final String url, final String destFileDir, final String destFileName, final OnDownloadListener listener) {

        Request request = new Request.Builder()
                .url(url)
                .build();

        //异步请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败监听回调
                listener.onDownloadFailed(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //储存下载文件的目录
                File dir = new File(destFileDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, destFileName);
                Sink sink = null;
                BufferedSink bufferedSink = null;
                try {
                    sink = Okio.sink(file);
                    bufferedSink = Okio.buffer(sink);
                    bufferedSink.writeAll(response.body().source());
                    bufferedSink.close();
                    listener.onDownloadSuccess(file);
                } catch (Exception e) {
                    log.error("error:{}", e);
                    listener.onDownloadFailed(e);
                }finally {
                    if(bufferedSink != null){
                        bufferedSink.close();
                    }
                }
            }
        });
    }

    public void downloadByAsync(final String url,final String[] headers, final String destFileDir, final String destFileName, final OnDownloadListener listener) {
        Request.Builder builder = new Request.Builder();
        if (headers != null && headers.length > 0) {
            if (headers.length % 2 == 0) {
                for (int i = 0; i < headers.length; i = i + 2) {
                    builder.addHeader(headers[i], headers[i + 1]);
                }
            } else {
                log.warn("headers's length[{}] is error.", headers.length);
            }

        }

        Request request = builder.url(url).build();
        //异步请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败监听回调
                listener.onDownloadFailed(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[4096];
                int len = 0;
                FileOutputStream fos = null;

                // 储存下载文件的目录
                File dir = new File(destFileDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, destFileName);

                try {
                    is = response.body().byteStream();
                    fos = new FileOutputStream(file);
                    int size = 0;
                    long total = response.body().contentLength();
                    while ((size = is.read(buf)) != -1) {
                        len += size;
                        fos.write(buf, 0, size);
                        int process = (int) Math.floor(((double) len / total) * 100);
                        // 控制台打印文件下载的百分比情况
                        listener.onDownloading(process);
                    }

                    fos.flush();
                    // 下载完成
                    listener.onDownloadSuccess(file);
                } catch (Exception e) {
                    log.error("error:{}", e);
                    listener.onDownloadFailed(e);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        });
    }


    public String downloadBySyscMerra2(final String url,final String[] headers, final String destFileDir, final String destFileName) {
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Host", "goldsmr4.gesdisc.eosdis.nasa.gov")
                .addHeader("Connection", "keep-alive")
                .addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"101\", \"Google Chrome\";v=\"101\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("DNT", "1")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.0.0 Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .addHeader("Sec-Fetch-Site", "none")
                .addHeader("Sec-Fetch-Mode", "navigate")
                .addHeader("Sec-Fetch-User", "?1")
                .addHeader("Sec-Fetch-Dest", "document")
                .addHeader("Accept-Encoding", "gzip, deflate, br")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9")
                .addHeader("Cookie", LAADSConstant.Cookie)
                .addHeader("Range", "bytes=6995968-407382484")
                .addHeader("If-Range", "\"184829d5-5eb4208df1fdf")
                .build();
        // 异步请求
        Response response = null;
        InputStream is = null;
        byte[] buf = new byte[4096];
        int len = 0;
        FileOutputStream fos = null;
        String res = null;

        try {
            response = okHttpClient.newCall(request).execute();

            if (response.isSuccessful()) {

                // 储存下载文件的目录
                File dir = new File(destFileDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, destFileName);

                is = response.body().byteStream();
                fos = new FileOutputStream(file);
                int size = 0;
                long total = response.body().contentLength();
                ConsoleProgressBar bar = new ConsoleProgressBar(50, '#');
                int progressbar = 0;
                while ((size = is.read(buf)) != -1) {
                    len += size;
                    fos.write(buf, 0, size);
                    int process = (int) Math.floor(((double) len / total) * 100);
                    if (process > progressbar) {
                        progressbar = process;
                        bar.show(progressbar);
                    }
                }
                fos.flush();
                res = destFileDir + File.separator + destFileName;
                return res;
            } else {
                throw new IOException("Unexpected code " + response);
            }
        } catch (IOException e) {
            log.error("error:{}", e);
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return res;
    }


    public String downloadBySysc(final String url, final String[] headers, final String destFileDir, final String destFileName) {
        String res = null;
        Request.Builder builder = new Request.Builder();
        if (headers != null && headers.length > 0) {
            if (headers.length % 2 == 0) {
                for (int i = 0; i < headers.length; i = i + 2) {
                    builder.addHeader(headers[i], headers[i + 1]);
                }
            } else {
                log.warn("headers's length[{}] is error.", headers.length);
            }

        }
        Request request = builder.url(url).build();
        // 异步请求
        Response response = null;
        InputStream is = null;
        byte[] buf = new byte[4096];
        int len = 0;
        FileOutputStream fos = null;

        try {
            response = okHttpClient.newCall(request).execute();

            if (response.isSuccessful()) {

                // 储存下载文件的目录
                File dir = new File(destFileDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, destFileName);

                is = response.body().byteStream();
                fos = new FileOutputStream(file);
                int size = 0;
                long total = response.body().contentLength();
                if(total < 1000 * 1000){ //如果网页返回值小于1mb ,则返回空
                    return "none";
                }else{
                    res = destFileDir + File.separator + destFileName;
                    ConsoleProgressBar bar = new ConsoleProgressBar(50, '#');
                    int progressbar = 0;
                    while ((size = is.read(buf)) != -1) {
                        len += size;
                        fos.write(buf, 0, size);
                        int process = (int) Math.floor(((double) len / total) * 100);
                        if (process > progressbar) {
                            progressbar = process;
                            bar.show(progressbar);
                        }
                    }
                    fos.flush();
                }
            } else {
                throw new IOException("Unexpected code " + response);
            }
        } catch (IOException e) {
            log.error("error:{}", e);
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return res;
    }


}
