package com.tencent.tmgp.car_physical;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

/**
     * 定义一个类，让其继承AsyncTask这个类
     * Params: String类型，表示传递给异步任务的参数类型是String，通常指定的是URL路径
     * Progress: Integer类型，进度条的单位通常都是Integer类型
     * Result：byte[]类型，表示我们下载好的图片以字节数组返回
     * @author xiaoluo
     *
     */
    public class DownloadFileAsyncTask extends AsyncTask<String, Integer, byte[]>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //    在onPreExecute()中我们让ProgressDialog显示出来

        }
        @Override
        protected byte[] doInBackground(String... params)
        {
            //    通过Apache的HttpClient来访问请求网络中的一张图片
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(params[0]);
            byte[] image = new byte[]{};
            try
            {
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                InputStream inputStream = null;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                if(httpEntity != null && httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                {
                    //    得到文件的总长度
                    long file_length = httpEntity.getContentLength();
                    //    每次读取后累加的长度
                    long total_length = 0;
                    int length = 0;
                    //    每次读取1024个字节
                    byte[] data = new byte[1024];
                    inputStream = httpEntity.getContent();
                    while(-1 != (length = inputStream.read(data)))
                    {
                        //    每读一次，就将total_length累加起来
                        total_length += length;
                        //    边读边写到ByteArrayOutputStream当中
                        byteArrayOutputStream.write(data, 0, length);
                        //    得到当前图片下载的进度
                        int progress = ((int)(total_length/(float)file_length) * 100);
                        //    时刻将当前进度更新给onProgressUpdate方法
                        publishProgress(progress);
                    }
                }
                image = byteArrayOutputStream.toByteArray();
                inputStream.close();
                byteArrayOutputStream.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                httpClient.getConnectionManager().shutdown();
            }
            return image;
        }
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
            //    更新ProgressDialog的进度条
        }
        @Override
        protected void onPostExecute(byte[] result)
        {
            super.onPostExecute(result);
            Log.i("onPostExecute", "" + result.length);
            //    将doInBackground方法返回的byte[]解码成要给Bitmap
           // Bitmap bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
            //    更新我们的ImageView控件
           // imageView.setImageBitmap(bitmap);
        }
    }
