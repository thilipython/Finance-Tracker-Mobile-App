package com.example.financetracker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AuthHelper {
    private static final String TAG = "AuthHelper";
    private final UserDao userDao;
    private final Executor executor;
    private final Handler mainHandler;

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface LogoutCallback {
        void onLogoutComplete();
        void onLogoutError(String message);
    }

    public interface RegistrationCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public AuthHelper(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.userDao = db.userDao();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void login(String email, String password, AuthCallback callback) {
        executor.execute(() -> {
            try {
                if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
                    notifyError(callback, "Email and password cannot be empty");
                    return;
                }

                String hashedPassword = SecurityUtils.hashPassword(password);
                User user = userDao.authenticate(email, hashedPassword);

                if (user != null) {
                    notifySuccess(callback, user);
                } else {
                    notifyError(callback, "Invalid credentials");
                }
            } catch (Exception e) {
                Log.e(TAG, "Login error", e);
                notifyError(callback, "Login failed: " + e.getMessage());
            }
        });
    }

    public void register(User user, RegistrationCallback callback) {
        executor.execute(() -> {
            try {
                if (user == null || user.email == null || user.email.isEmpty() || user.password == null || user.password.isEmpty()) {
                    notifyRegistrationError(callback, "Invalid user data");
                    return;
                }

                // Check if email already exists
                User existingUser = userDao.getUserByEmail(user.email);
                if (existingUser != null) {
                    notifyRegistrationError(callback, "Email already registered");
                    return;
                }

                // Hash password before storing
                user.password = SecurityUtils.hashPassword(user.password);
                long userId = userDao.insert(user);

                if (userId > 0) {
                    user.id = (int) userId;
                    notifyRegistrationSuccess(callback, user);
                } else {
                    notifyRegistrationError(callback, "Registration failed");
                }
            } catch (Exception e) {
                Log.e(TAG, "Registration error", e);
                notifyRegistrationError(callback, "Registration failed: " + e.getMessage());
            }
        });
    }

    public void logout(LogoutCallback callback) {
        executor.execute(() -> {
            try {
                // Perform any cleanup operations here
                // For example: userDao.clearSessionData();

                // Simulate logout process
                Thread.sleep(500); // Remove this in production

                mainHandler.post(() -> callback.onLogoutComplete());
            } catch (Exception e) {
                Log.e(TAG, "Logout error", e);
                mainHandler.post(() -> callback.onLogoutError("Logout failed: " + e.getMessage()));
            }
        });
    }

    public void checkEmailExists(String email, RegistrationCallback callback) {
        executor.execute(() -> {
            try {
                User existingUser = userDao.getUserByEmail(email);
                if (existingUser != null) {
                    notifyRegistrationError(callback, "Email already registered");
                } else {
                    notifyRegistrationSuccess(callback, null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Email check error", e);
                notifyRegistrationError(callback, "Error checking email availability");
            }
        });
    }

    private void notifySuccess(AuthCallback callback, User user) {
        mainHandler.post(() -> callback.onSuccess(user));
    }

    private void notifyError(AuthCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    private void notifyRegistrationSuccess(RegistrationCallback callback, User user) {
        mainHandler.post(() -> callback.onSuccess(user));
    }

    private void notifyRegistrationError(RegistrationCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}