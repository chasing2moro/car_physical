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
     * ����һ���࣬����̳�AsyncTask�����
     * Params: String���ͣ���ʾ���ݸ��첽����Ĳ���������String��ͨ��ָ������URL·��
     * Progress: Integer���ͣ��������ĵ�λͨ������Integer����
     * Result��byte[]���ͣ���ʾ�������غõ�ͼƬ���ֽ����鷵��
     * @author xiaoluo
     *
     */
    public class DownloadFileAsyncTask extends AsyncTask<String, Integer, byte[]>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //    ��onPreExecute()��������ProgressDialog��ʾ����

        }
        @Override
        protected byte[] doInBackground(String... params)
        {
            //    ͨ��Apache��HttpClient���������������е�һ��ͼƬ
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
                    //    �õ��ļ����ܳ���
                    long file_length = httpEntity.getContentLength();
                    //    ÿ�ζ�ȡ���ۼӵĳ���
                    long total_length = 0;
                    int length = 0;
                    //    ÿ�ζ�ȡ1024���ֽ�
                    byte[] data = new byte[1024];
                    inputStream = httpEntity.getContent();
                    while(-1 != (length = inputStream.read(data)))
                    {
                        //    ÿ��һ�Σ��ͽ�total_length�ۼ�����
                        total_length += length;
                        //    �߶���д��ByteArrayOutputStream����
                        byteArrayOutputStream.write(data, 0, length);
                        //    �õ���ǰͼƬ���صĽ���
                        int progress = ((int)(total_length/(float)file_length) * 100);
                        //    ʱ�̽���ǰ���ȸ��¸�onProgressUpdate����
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
            //    ����ProgressDialog�Ľ�����
        }
        @Override
        protected void onPostExecute(byte[] result)
        {
            super.onPostExecute(result);
            Log.i("onPostExecute", "" + result.length);
            //    ��doInBackground�������ص�byte[]�����Ҫ��Bitmap
           // Bitmap bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
            //    �������ǵ�ImageView�ؼ�
           // imageView.setImageBitmap(bitmap);
        }
    }
