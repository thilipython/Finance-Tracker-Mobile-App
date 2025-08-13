package com.example.financetracker;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class SpendingPredictor {
    private final Interpreter tflite;
    private static final float SCALE_FACTOR = 1500f;
    private static final int MONTHS_TO_CONSIDER = 3;

    public SpendingPredictor(Context context) throws IOException, IllegalArgumentException {
        try {
            tflite = new Interpreter(loadModelFile(context));
        } catch (IOException e) {
            throw new IOException("Failed to load TFLite model", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Model initialization failed", e);
        }
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = null;
        FileInputStream inputStream = null;
        try {
            fileDescriptor = context.getAssets().openFd("spending_model.tflite");
            inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (fileDescriptor != null) {
                fileDescriptor.close();
            }
        }
    }

    public synchronized float predictNextMonthSpending(List<Transaction> pastTransactions) {
        if (pastTransactions == null || pastTransactions.isEmpty()) {
            return 0f;
        }

        try {

            float[][] input = new float[1][1];
            input[0][0] = calculateWeightedAverage(pastTransactions) / SCALE_FACTOR;


            float[][] output = new float[1][1];
            tflite.run(input, output);


            return output[0][0] * SCALE_FACTOR;
        } catch (Exception e) {
            return 0f;
        }
    }

    private float calculateWeightedAverage(List<Transaction> transactions) {
        float sum = 0f;
        float weightSum = 0f;
        int count = Math.min(transactions.size(), MONTHS_TO_CONSIDER);


        for (int i = 0; i < count; i++) {
            float weight = (count - i) * 0.5f;
            sum += Math.abs(transactions.get(i).amount) * weight;
            weightSum += weight;
        }

        return weightSum > 0 ? sum / weightSum : 0f;
    }

    public void close() {
        if (tflite != null) {
            tflite.close();
        }
    }
}