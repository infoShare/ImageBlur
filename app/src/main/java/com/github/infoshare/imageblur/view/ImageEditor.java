package com.github.infoshare.imageblur.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.*;
import com.aviadmini.quickimagepick.PickCallback;
import com.aviadmini.quickimagepick.PickSource;
import com.aviadmini.quickimagepick.QiPick;
import com.github.infoshare.imageblur.R;
import com.github.infoshare.imageblur.component.DrawableView;
import com.github.infoshare.imageblur.model.DrawMode;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static com.github.infoshare.imageblur.security.AppPermissions.SAVE_FILE;
import static com.github.infoshare.imageblur.security.AppPermissions.SHARE_IMAGE;

public class ImageEditor extends AppCompatActivity implements PickCallback{

    private final String ERROR_TEXT = "Error occurred. Please try again later.";
    private DrawableView drawableView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editor);
        initControls();
    }

    private void initControls() {
        drawableView = (DrawableView) findViewById(R.id.imageView);
        initDrawMode();
        initSizeBar();
    }

    private void initSizeBar() {
        SeekBar sizeBar = (SeekBar) findViewById(R.id.sizeBar);
        sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                drawableView.setSize(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initDrawMode() {
        Spinner drawModeSpinner = (Spinner) findViewById(R.id.drawMode);
        drawModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                drawableView.setDrawMode(DrawMode.valueOf((String) adapterView.getSelectedItem()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}

        });
    }

    public void openImage(View view) {
        QiPick.in(this).fromMultipleSources("Choose source",
                PickSource.CAMERA, PickSource.DOCUMENTS, PickSource.GALLERY);
    }

    public void shareImage(View view) {
        tryToShareImage();
    }

    private void tryToShareImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, ERROR_TEXT, Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SHARE_IMAGE);
            }
        } else {
            shareImage();
        }
    }

    private void shareImage() {
        Bitmap image = getCurrentImage();
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), image,"Image Blur", null);
        Uri uri = Uri.parse(path);
        final Intent action = new Intent(Intent.ACTION_SEND);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        action.putExtra(Intent.EXTRA_STREAM, uri);
        action.setType("image/png");
        startActivity(action);
    }

    public void saveImage(View view) {
        tryToSaveFile();
    }

    private void tryToSaveFile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, ERROR_TEXT, Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SAVE_FILE);
            }
        } else {
            saveFile();
        }
    }

    private void saveFile() {
        Bitmap image = getCurrentImage();
        try {
            String fileName = System.currentTimeMillis() / 1000 + "_blur.png";
            File imageFile = createImageFile(fileName);
            saveToFile(image, imageFile);
            addFileToGallery(imageFile);
            Toast.makeText(this, "Image saved to " + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, ERROR_TEXT, Toast.LENGTH_SHORT).show();
        }
    }

    private void addFileToGallery(File imageFile) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void saveToFile(Bitmap bitmap, File imageFile) throws IOException {
        OutputStream fOut = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        fOut.flush();
        fOut.close();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File createImageFile(String fileName) throws IOException {
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Imag Blur");
        storageDir.mkdir();
        File imageFile = new File(storageDir, fileName);
        imageFile.createNewFile();
        return imageFile;
    }

    private Bitmap getCurrentImage() {
        drawableView.buildDrawingCache();
        return drawableView.getDrawingCache();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case SAVE_FILE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveFile();
                } else {
                    Toast.makeText(this, "File cannot be saved", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case SHARE_IMAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    shareImage();
                } else {
                    Toast.makeText(this, "File cannot be shared", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void clearImage(View view) {
        drawableView.clearImage();
    }

    @Override
    protected void onActivityResult(final int pRequestCode, final int pResultCode, final Intent pData) {
        super.onActivityResult(pRequestCode, pResultCode, pData);
        QiPick.handleActivityResult(getApplicationContext(), pRequestCode, pResultCode, pData, this);
    }

    //ImagePick callback
    @Override
    public void onImagePicked(@NonNull final PickSource pPickSource, final int pRequestType, @NonNull final Uri pImageUri) {
        Picasso.with(getApplicationContext())
                .load(pImageUri)
                .resize(drawableView.getWidth(), drawableView.getHeight())
                .centerInside()
                .into(drawableView);
    }

    @Override
    public void onMultipleImagesPicked(final int pRequestType, @NonNull final List<Uri> pImageUris) {
        this.onImagePicked(PickSource.DOCUMENTS, pRequestType, pImageUris.get(0));
    }

    @Override
    public void onError(@NonNull final PickSource pPickSource, final int pRequestType, @NonNull final String pErrorString) {
        //Toast.makeText(getApplicationContext(), "OnError", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel(@NonNull final PickSource pPickSource, final int pRequestType) {
        //Toast.makeText(getApplicationContext(), "OnCancel", Toast.LENGTH_SHORT).show();
    }

    public void undoDraw(View view) {
        drawableView.undoDraw();
    }
}
