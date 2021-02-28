# S3upload
사진으로 암기하는 영어 단어 암기 어플리케이션

먼저 이걸 하기 위해선

1. AWS_REKOGNITION 사용법을 잘 알아야 한다.

2. 촬영한 사진을 저장하는 법을 알아야한다. <- 내가 저번에 이걸 못했지

3. 갤러리에서 선택한 사진, 바로 직접 촬영한 사진에 대해 AWS_REKOGNITION 을 해야한다.

4. 이미지에서 추출한 단어에서 중복을 배제
   1. 이 때 중복된 개체들을 복수형으로 알려주고 싶기도 하다. (선택형 옵션)
   
5. 단어 암기 이외에 단어에 대한 음성을 사용자가 이야기 할 시 그걸 평가해서 점수로 알려 주고 싶다. <- 이건 파이썬으로 해봤는데 자바로는 처음이다.

## 필요한것

1. 가은언니한테 부탁해서라도 AWS_REKOGNTION 을 사용하는 걸 배워야한다.

2. ~~촬영한 사진 대체 어찌 저장하지???~~ (2021-02-15 경로 재설정으로 카메라 촬영사진 확인 완료)

3. 사진을 EC2 에 올리는게 아니라 바로바로 LAMBDA 를 돌려야하나

4. "갤러리에서 선택한 사진, 바로 직접 촬영한 사진에 대해 AWS_REKOGNITION 을 해야한다." 이 부분 너무 간단하게 말했지만 이게 문제다. 다시 잘 생각해보자.

5. 내 폰 하나에서만 돌릴 게 아니라, 다른 모든 유저들이 요청한 사진들 각각에 대해서 처리 해줘야 한다. <br> <- 이걸 어떻게 해야 잘 할 수 있을까? 다른 누군가에게 물어보자


## 현재 에러 발생 기록

```Java

2021-02-28 19:07:39.085 18502-18502/com.example.etri E/AndroidRuntime: FATAL EXCEPTION: main
    Process: com.example.etri, PID: 18502
    java.lang.NullPointerException: Attempt to invoke virtual method 'com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility.upload(java.lang.String, java.lang.String, java.io.File)' on a null object reference
        at com.example.etri.MainActivity.onClick(MainActivity.java:164)
        at android.view.View.performClick(View.java:7862)
        at android.widget.TextView.performClick(TextView.java:15004)
        at android.view.View.performClickInternal(View.java:7831)
        at android.view.View.access$3600(View.java:879)
        at android.view.View$PerformClick.run(View.java:29359)
        at android.os.Handler.handleCallback(Handler.java:883)
        at android.os.Handler.dispatchMessage(Handler.java:100)
        at android.os.Looper.loop(Looper.java:237)
        at android.app.ActivityThread.main(ActivityThread.java:8167)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:496)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1100)

```