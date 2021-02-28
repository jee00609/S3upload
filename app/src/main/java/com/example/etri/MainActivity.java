package com.example.etri;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;

//
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final String TAG = getClass().getSimpleName();

    final String LogTag = "LogTag";

    ImageView imageView;
    Button cameraBtn;
    Button uploadBtn;

    final static int TAKE_PICTURE = 1;

    String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;

    ////////

    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3 s3;
    TransferUtility transferUtility;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 레이아웃과 변수 연결
        imageView = findViewById(R.id.imageview);
        cameraBtn = findViewById(R.id.camera_button);

        uploadBtn = findViewById(R.id.uploadBtn);

        // 카메라 버튼에 리스터 추가
        cameraBtn.setOnClickListener(this);

        // 서버 업로드 버튼에 리스너 추가
        uploadBtn.setOnClickListener(this);

        // 6.0 마쉬멜로우 이상일 경우에는 권한 체크 후 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {
                Log.d(TAG, "권한 설정 완료");
            } else {
                Log.d(TAG, "권한 설정 요청");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        // Amazon Cognito 인증 공급자를 초기화합니다
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "자격 증명 풀", // 자격 증명 풀 ID
                Regions.US_EAST_1 // 리전
        );

        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        s3.setEndpoint("s3.us-east-1.amazonaws.com");
        System.out.println("확인해보기 1111111111");

        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

        //초기화
        transferUtility.cancel(Integer.MAX_VALUE - 1);

//        /**
//         * work around for a bug:
//         * http://stackoverflow.com/questions/36587511/android-amazon-s3-uploading-crash
//         */
//        public static void startupTranferServiceEarlyToAvoidBugs(Context context) {
//            final TransferUtility tu = new TransferUtility(
//                    new AmazonS3Client((AWSCredentials)null),
//                    context);
//            tu.cancel(Integer.MAX_VALUE - 1);
//        }

    }

    // 권한 요청
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
            Log.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

    // 버튼 onClick리스터 처리부분
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.camera_button:
                // 카메라 앱을 여는 소스
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, TAKE_PICTURE);
//                break;
                // 카메라 앱을 여는 함수
                dispatchTakePictureIntent();
                break;

            case R.id.uploadBtn:

                uploadPicture();

                break;

        }
    }


    private void uploadPicture(){

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        System.out.println("확인해보기 2.5 "+imageFileName);

        File fileToUpload = new File(mCurrentPhotoPath);

        String testPath = "/storage/emulated/0/DCIM/Screenshots/20200806_113923.jpg";

//        File fileToUpload = new File(testPath);
        System.out.println("확인해보기 33333333 "+fileToUpload);

        if (fileToUpload == null) {
            Toast.makeText(this, "Could not find the filepath of  the selected file", Toast.LENGTH_LONG).show();

            System.out.println("존재 하지 않아요!");
            // to make sure that file is not emapty or null
            return;
        }
        if (fileToUpload != null){
            Toast.makeText(this, "Could find the filepath of  the selected file", Toast.LENGTH_LONG).show();
            System.out.println("존재 해요!");
        }

        //rekogntion 이 아니라 rekognition 이다! 멍청아! 나중에 만들때는 s3 버킷 이름을 고쳐서 완성하자
        TransferObserver observer = transferUtility.upload(
                "android-rekogntion", /* 업로드 할 버킷 이름 */
                imageFileName, /* 버킷에 저장할 파일의 이름 */
                fileToUpload /* 버킷에 저장할 파일 */
        );

        transferObserverListener(observer);


    }

    public void transferObserverListener(TransferObserver transferObserver){
        transferObserver.setTransferListener(new TransferListener(){
            @Override
            public void onStateChanged(int id, TransferState state) {
                Toast.makeText(getApplicationContext(), "State Change" + state,
                        Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent/bytesTotal * 100);
                Toast.makeText(getApplicationContext(), "Progress in %" + percentage,
                        Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(int id, Exception ex) {
                Log.e("error","error");
            }
        });
    }

    // 카메라로 촬영한 이미지를 가져오는 부분
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO: {
                    if (resultCode == RESULT_OK) {
                        File file = new File(mCurrentPhotoPath);
                        Bitmap bitmap = MediaStore.Images.Media
                                .getBitmap(getContentResolver(), Uri.fromFile(file));
                        if (bitmap != null) {
                            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED);

                            Bitmap rotatedBitmap = null;
                            switch(orientation) {

                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotatedBitmap = rotateImage(bitmap, 90);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotatedBitmap = rotateImage(bitmap, 180);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotatedBitmap = rotateImage(bitmap, 270);
                                    break;

                                case ExifInterface.ORIENTATION_NORMAL:
                                default:
                                    rotatedBitmap = bitmap;
                            }

                            galleryAddPic();

                            imageView.setImageBitmap(rotatedBitmap);
                        }
                    }
                    break;
                }

            }

        } catch (Exception error) {
            error.printStackTrace();
        }


    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.etri.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    // 갤러리 저장
    // Adroid data com.example.etri files Pictures 폴더에 저장된다.
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

        Toast toast=Toast.makeText(MainActivity.this,"GalleryAddPic",Toast.LENGTH_SHORT);
        toast.show();

    }

}
