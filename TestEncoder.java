package edu.sjtu.zhusy54.qrcode;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Created by Syman-Z on 2016/2/25.
 */
public class TestEncoder extends Activity {
    EditText textContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encoder);

        textContent = (EditText) findViewById(R.id.gen_content);
        final Button genBtn = (Button) findViewById(R.id.btn_generate);
        genBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String contentString = textContent.getText().toString();
                    if (!contentString.equals("")) {
                        Hashtable hints = new Hashtable();
                        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

                        BitMatrix matrix = new QRCodeWriter().encode(contentString, BarcodeFormat.QR_CODE, 400, 400,hints);
//                        BitMatrix matrix = new MultiFormatWriter().encode(contentString,
//                                BarcodeFormat.QR_CODE, 300, 300);
                        int width = matrix.getWidth();
                        int height = matrix.getHeight();
                        int[] pixels = new int[width * height];

                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                if (matrix.get(x, y)) {
                                    pixels[y * width + x] = Color.BLACK;
                                }
                                else{
                                    pixels[y * width + x] = Color.WHITE;
                                }
                            }
                        }
                        final Bitmap bitmap = Bitmap.createBitmap(width, height,
                                Bitmap.Config.ARGB_8888);
                        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
                        final ImageView image1 = new ImageView(TestEncoder.this);
                        image1.setImageBitmap(bitmap);
                        new AlertDialog.Builder(TestEncoder.this)
                                .setTitle("QR Code")
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setView(image1)
                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
//                        Bitmap qrCodeBitmap = EncodingHandler.createQRCode(contentString, 350);
//                        qrImgImageView.setImageBitmap(qrCodeBitmap);
                        final String[] items = new String[]{"保存图片"};
                        image1.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(TestEncoder.this);
                                builder.setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Bitmap bmp = ((BitmapDrawable) image1.getDrawable()).getBitmap();
                                        if(bmp == null){
                                            Toast.makeText(TestEncoder.this, "图片为空，保存失败", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            saveImageToGallery(TestEncoder.this,bmp);
                                        }
                                    }
                                });
                                builder.show();
                                return true;
                            }
                        });
                    } else {
                        Toast.makeText(TestEncoder.this, "Text can not be empty", Toast.LENGTH_SHORT).show();
                    }

                } catch (WriterException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

    }

    private void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片 创建文件夹
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    TestEncoder.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        File appDir = new File(Environment.getExternalStorageDirectory().getPath()+"/QRcode");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        //图片文件名称
        String fileName = "xfc.jpg";
        File file = new File(appDir, fileName);
        try {
            //Toast.makeText(context, "保存成功2", Toast.LENGTH_SHORT).show();
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            Log.e("111", e.getMessage());
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        String path = file.getAbsolutePath();
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), path, fileName, null);
        } catch (FileNotFoundException e) {
            Log.e("333", e.getMessage());
            e.printStackTrace();
        }
        // 最后通知图库更新
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
        Toast.makeText(context, path+"保存成功", Toast.LENGTH_SHORT).show();
    }
}
