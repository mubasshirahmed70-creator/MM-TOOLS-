<?php
/**
 * MM Tools - Web Admin Control Panel
 * Allows real-time remote control of Maintenance Mode, Force Join Telegram, and App Updates.
 */
session_start();

// ====== SECURITY CONFIGURATION ======
// Change this password to whatever you like!
define('ADMIN_PASSWORD', 'mmtools70');
$configFile = __DIR__ . '/app-config.json';

// Handle Logout
if (isset($_GET['action']) && $_GET['action'] === 'logout') {
    session_destroy();
    header("Location: admin.php");
    exit;
}

// Handle Login
$loginError = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['login_password'])) {
    if ($_POST['login_password'] === ADMIN_PASSWORD) {
        $_SESSION['admin_logged_in'] = true;
        header("Location: admin.php");
        exit;
    } else {
        $loginError = 'ভুল পাসওয়ার্ড! আবার চেষ্টা করুন।';
    }
}

// Check if user is logged in
$isLoggedIn = !empty($_SESSION['admin_logged_in']);

// If not logged in, show Login Screen
if (!$isLoggedIn) {
?>
<!DOCTYPE html>
<html lang="bn">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MM Tools - Admin Login</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, sans-serif; }
        body { background: #0B111E; color: #E2E8F0; display: flex; align-items: center; justify-content: center; min-height: 100vh; padding: 20px; }
        .login-card { background: #131E32; border: 1px solid #1E293B; border-radius: 16px; padding: 32px 24px; width: 100%; max-width: 380px; box-shadow: 0 10px 30px rgba(0,0,0,0.5); text-align: center; }
        .logo-badge { width: 56px; height: 56px; border-radius: 14px; background: linear-gradient(135deg, #00E5FF, #2979FF); display: inline-flex; align-items: center; justify-content: center; font-size: 24px; font-weight: bold; color: #0B111E; margin-bottom: 16px; box-shadow: 0 0 20px rgba(0, 229, 255, 0.4); }
        h1 { font-size: 20px; font-weight: 700; margin-bottom: 6px; color: #FFFFFF; }
        p.subtitle { font-size: 13px; color: #94A3B8; margin-bottom: 24px; }
        .input-group { margin-bottom: 20px; text-align: left; }
        label { display: block; font-size: 12px; font-weight: 600; color: #94A3B8; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.5px; }
        input[type="password"] { width: 100%; padding: 14px 16px; background: #0B111E; border: 1.5px solid #1E293B; border-radius: 10px; color: #FFF; font-size: 15px; outline: none; transition: border-color 0.2s; }
        input[type="password"]:focus { border-color: #00E5FF; }
        button.btn-login { width: 100%; padding: 14px; background: linear-gradient(135deg, #00E5FF, #00B0FF); color: #0B111E; border: none; border-radius: 10px; font-size: 15px; font-weight: 700; cursor: pointer; transition: transform 0.1s, opacity 0.2s; }
        button.btn-login:active { transform: scale(0.98); }
        .error-msg { background: rgba(255, 23, 68, 0.15); border: 1px solid #FF1744; color: #FF5252; padding: 10px; border-radius: 8px; font-size: 13px; margin-bottom: 16px; }
    </style>
</head>
<body>
    <div class="login-card">
        <div class="logo-badge">MM</div>
        <h1>MM Tools Control</h1>
        <p class="subtitle">লগইন করতে অ্যাডমিন পাসওয়ার্ড দিন</p>
        <?php if (!empty($loginError)): ?>
            <div class="error-msg"><?php echo htmlspecialchars($loginError); ?></div>
        <?php endif; ?>
        <form method="POST" action="admin.php">
            <div class="input-group">
                <label>অ্যাডমিন পাসওয়ার্ড</label>
                <input type="password" name="login_password" placeholder="••••••••" required autofocus>
            </div>
            <button type="submit" class="btn-login">লগইন করুন</button>
        </form>
    </div>
</body>
</html>
<?php
    exit;
}

// Default Configuration values
$defaults = [
    'min_required_version' => 1,
    'latest_version' => '1.0',
    'force_update' => false,
    'is_app_disabled' => false,
    'disabled_title' => 'Maintenance Mode',
    'disabled_message' => 'MM Tools সাময়িকভাবে মেইনটেনেন্সের জন্য বন্ধ রয়েছে। অনুগ্রহ করে কিছুক্ষণ পর আবার চেষ্টা করুন।',
    'update_title' => 'Update Required',
    'update_message' => 'MM Tools-এর একটি নতুন ভার্সন এসেছে। নতুন ফিচার ব্যবহার করতে এখনই আপডেট করুন।',
    'download_url' => 'https://t.me/mmtechnical420',
    'telegram_url' => 'https://t.me/mmtechnical420',
    'force_join_telegram' => false,
    'force_join_title' => 'টেলিগ্রাম চ্যানেলে জয়েন করুন',
    'force_join_message' => 'আমাদের অফিসিয়াল চ্যানেলে জয়েন করুন সকল নতুন আপডেট, ট্রিকস এবং সাপোর্ট সবার আগে পেতে!',
    'force_join_url' => 'https://t.me/mmtechnical420',
    'force_join_dismissible' => true
];

// Load existing config or initialize defaults
$config = $defaults;
if (file_exists($configFile)) {
    $existing = json_decode(file_get_contents($configFile), true);
    if (is_array($existing)) {
        $config = array_merge($defaults, $existing);
    }
}

// Handle Form Submission (Save Settings)
$successMessage = '';
$errorMessage = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['save_settings'])) {
    $newConfig = [
        'min_required_version' => max(1, intval($_POST['min_required_version'] ?? 1)),
        'latest_version' => trim($_POST['latest_version'] ?? '1.0'),
        'force_update' => isset($_POST['force_update']),
        'is_app_disabled' => isset($_POST['is_app_disabled']),
        'disabled_title' => trim($_POST['disabled_title'] ?? 'Maintenance Mode'),
        'disabled_message' => trim($_POST['disabled_message'] ?? ''),
        'update_title' => trim($_POST['update_title'] ?? 'Update Required'),
        'update_message' => trim($_POST['update_message'] ?? ''),
        'download_url' => trim($_POST['download_url'] ?? ''),
        'telegram_url' => trim($_POST['telegram_url'] ?? ''),
        'force_join_telegram' => isset($_POST['force_join_telegram']),
        'force_join_title' => trim($_POST['force_join_title'] ?? 'টেলিগ্রাম চ্যানেলে জয়েন করুন'),
        'force_join_message' => trim($_POST['force_join_message'] ?? ''),
        'force_join_url' => trim($_POST['force_join_url'] ?? ''),
        'force_join_dismissible' => ($_POST['force_join_type'] ?? 'optional') === 'optional'
    ];

    $jsonData = json_encode($newConfig, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    if (file_put_contents($configFile, $jsonData) !== false) {
        $config = $newConfig;
        $successMessage = 'সেটিংস সফলভাবে সংরক্ষিত হয়েছে! অ্যাপে ১ সেকেন্ডের মধ্যে কার্যকর হবে। 🚀';
    } else {
        $errorMessage = 'ফাইল সেভ করা যায়নি! ফাইল পারমিশন চেক করুন (public_html/app-config.json)।';
    }
}
?>
<!DOCTYPE html>
<html lang="bn">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MM Tools - Web Admin Panel</title>
    <style>
        :root {
            --bg-dark: #070B14;
            --card-bg: #10192A;
            --card-border: #1E293B;
            --accent-cyan: #00E5FF;
            --accent-blue: #2979FF;
            --danger-red: #FF1744;
            --warning-amber: #FFB300;
            --success-green: #00E676;
            --text-main: #F8FAFC;
            --text-muted: #94A3B8;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, sans-serif; }
        body { background: var(--bg-dark); color: var(--text-main); padding-bottom: 50px; }
        header { background: #0E1626; border-bottom: 1px solid var(--card-border); padding: 14px 20px; display: flex; justify-content: space-between; align-items: center; position: sticky; top: 0; z-index: 100; }
        .brand { display: flex; align-items: center; gap: 10px; }
        .brand-badge { width: 34px; height: 34px; border-radius: 9px; background: linear-gradient(135deg, var(--accent-cyan), var(--accent-blue)); color: #070B14; font-weight: 800; display: flex; align-items: center; justify-content: center; font-size: 15px; }
        .brand-title { font-size: 16px; font-weight: 700; color: #FFF; }
        .brand-title span { color: var(--accent-cyan); }
        .header-actions { display: flex; gap: 8px; }
        .btn-header { padding: 6px 12px; border-radius: 8px; font-size: 12px; font-weight: 600; text-decoration: none; display: inline-flex; align-items: center; gap: 4px; transition: 0.2s; }
        .btn-view-json { background: rgba(0, 229, 255, 0.1); color: var(--accent-cyan); border: 1px solid rgba(0, 229, 255, 0.3); }
        .btn-logout { background: rgba(255, 23, 68, 0.1); color: #FF5252; border: 1px solid rgba(255, 23, 68, 0.3); }
        
        .container { max-width: 680px; margin: 20px auto; padding: 0 16px; }
        
        .alert { padding: 14px 18px; border-radius: 12px; font-size: 14px; margin-bottom: 20px; display: flex; align-items: center; gap: 10px; }
        .alert-success { background: rgba(0, 230, 118, 0.15); border: 1px solid var(--success-green); color: var(--success-green); }
        .alert-error { background: rgba(255, 23, 68, 0.15); border: 1px solid var(--danger-red); color: #FF5252; }
        
        .card { background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 16px; padding: 22px; margin-bottom: 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.3); }
        .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; padding-bottom: 12px; border-bottom: 1px solid rgba(255,255,255,0.06); }
        .card-title { display: flex; align-items: center; gap: 10px; font-size: 16px; font-weight: 700; color: #FFF; }
        .card-icon { font-size: 20px; }
        
        /* Toggle Switch */
        .switch-container { display: flex; align-items: center; gap: 10px; cursor: pointer; }
        .switch { position: relative; display: inline-block; width: 50px; height: 26px; }
        .switch input { opacity: 0; width: 0; height: 0; }
        .slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #2D3748; transition: .3s; border-radius: 26px; border: 1px solid #4A5568; }
        .slider:before { position: absolute; content: ""; height: 18px; width: 18px; left: 3px; bottom: 3px; background-color: white; transition: .3s; border-radius: 50%; }
        input:checked + .slider { background-color: var(--accent-cyan); border-color: var(--accent-cyan); }
        input:checked + .slider:before { transform: translateX(24px); background-color: #070B14; }
        .switch-label { font-size: 13px; font-weight: 700; text-transform: uppercase; }
        .switch-label.on { color: var(--accent-cyan); }
        .switch-label.off { color: var(--text-muted); }
        
        /* Form Inputs */
        .form-group { margin-bottom: 16px; }
        label { display: block; font-size: 13px; font-weight: 600; color: var(--text-muted); margin-bottom: 6px; }
        input[type="text"], input[type="number"], textarea, select { width: 100%; padding: 12px 14px; background: #070B14; border: 1.5px solid var(--card-border); border-radius: 10px; color: #FFF; font-size: 14px; outline: none; transition: border-color 0.2s; }
        input[type="text"]:focus, input[type="number"]:focus, textarea:focus, select:focus { border-color: var(--accent-cyan); }
        textarea { resize: vertical; min-height: 80px; line-height: 1.5; }
        .input-hint { font-size: 11px; color: #64748B; margin-top: 4px; }
        
        .row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
        @media (max-width: 480px) { .row-2 { grid-template-columns: 1fr; } }
        
        .btn-save-sticky { position: sticky; bottom: 16px; z-index: 90; }
        button.btn-save { width: 100%; padding: 16px; background: linear-gradient(135deg, var(--accent-cyan), #00B0FF); color: #070B14; border: none; border-radius: 14px; font-size: 16px; font-weight: 800; cursor: pointer; box-shadow: 0 6px 20px rgba(0, 229, 255, 0.4); display: flex; align-items: center; justify-content: center; gap: 8px; transition: transform 0.1s; }
        button.btn-save:active { transform: scale(0.98); }
    </style>
</head>
<body>

<header>
    <div class="brand">
        <div class="brand-badge">MM</div>
        <div class="brand-title">MM Tools <span>Admin</span></div>
    </div>
    <div class="header-actions">
        <a href="app-config.json" target="_blank" class="btn-header btn-view-json">🔗 View JSON</a>
        <a href="admin.php?action=logout" class="btn-header btn-logout">লগআউট</a>
    </div>
</header>

<div class="container">
    <?php if (!empty($successMessage)): ?>
        <div class="alert alert-success">✅ <?php echo htmlspecialchars($successMessage); ?></div>
    <?php endif; ?>
    <?php if (!empty($errorMessage)): ?>
        <div class="alert alert-error">❌ <?php echo htmlspecialchars($errorMessage); ?></div>
    <?php endif; ?>

    <form method="POST" action="admin.php">
        
        <!-- SECTION 1: MAINTENANCE MODE -->
        <div class="card" style="border-left: 4px solid var(--danger-red);">
            <div class="card-header">
                <div class="card-title">
                    <span class="card-icon">🛠️</span>
                    <span>মেইনটেনেন্স মোড (Maintenance Mode)</span>
                </div>
                <div class="switch-container">
                    <label class="switch">
                        <input type="checkbox" name="is_app_disabled" <?php echo !empty($config['is_app_disabled']) ? 'checked' : ''; ?>>
                        <span class="slider"></span>
                    </label>
                </div>
            </div>
            
            <div class="form-group">
                <label>নোটিশ টাইটেল (Maintenance Title)</label>
                <input type="text" name="disabled_title" value="<?php echo htmlspecialchars($config['disabled_title'] ?? 'Maintenance Mode'); ?>" placeholder="যেমন: Maintenance Mode / সার্ভার রক্ষণাবেক্ষণ">
            </div>

            <div class="form-group">
                <label>মেইনটেনেন্স মেসেজ (Maintenance Message)</label>
                <textarea name="disabled_message" placeholder="ইউজারদের কি মেসেজ দেখাবেন এখানে লিখুন..."><?php echo htmlspecialchars($config['disabled_message'] ?? ''); ?></textarea>
                <div class="input-hint">টিপস: এই মোড চালু থাকলে ব্যবহারকারী অ্যাপ ওপেন করলে এই মেসেজ দেখবে এবং অ্যাপ ব্যবহার আটকে থাকবে।</div>
            </div>

            <div class="form-group">
                <label>টেলিগ্রাম সাপোর্ট লিংক (Maintenance Telegram Support)</label>
                <input type="text" name="telegram_url" value="<?php echo htmlspecialchars($config['telegram_url'] ?? 'https://t.me/mmtechnical420'); ?>">
            </div>
        </div>

        <!-- SECTION 2: FORCE JOIN TELEGRAM -->
        <div class="card" style="border-left: 4px solid var(--accent-cyan);">
            <div class="card-header">
                <div class="card-title">
                    <span class="card-icon">📢</span>
                    <span>ফোর্স জয়েন টেলিগ্রাম (Force Telegram Join)</span>
                </div>
                <div class="switch-container">
                    <label class="switch">
                        <input type="checkbox" name="force_join_telegram" <?php echo !empty($config['force_join_telegram']) ? 'checked' : ''; ?>>
                        <span class="slider"></span>
                    </label>
                </div>
            </div>

            <div class="row-2">
                <div class="form-group">
                    <label>পপআপের ধরন (Join Dialog Type)</label>
                    <select name="force_join_type">
                        <option value="optional" <?php echo (!empty($config['force_join_dismissible'])) ? 'selected' : ''; ?>>ঐচ্ছিক / স্কিপযোগ্য (Skip Allowed)</option>
                        <option value="mandatory" <?php echo (empty($config['force_join_dismissible'])) ? 'selected' : ''; ?>>বাধ্যতামূলক (Strict Force Join)</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>পপআপ টাইটেল (Join Title)</label>
                    <input type="text" name="force_join_title" value="<?php echo htmlspecialchars($config['force_join_title'] ?? 'টেলিগ্রাম চ্যানেলে জয়েন করুন'); ?>">
                </div>
            </div>

            <div class="form-group">
                <label>ফোর্স জয়েন মেসেজ (Join Message)</label>
                <textarea name="force_join_message" placeholder="চ্যানেলে জয়েন করার জন্য ইউজারকে কি বলতে চান লিখুন..."><?php echo htmlspecialchars($config['force_join_message'] ?? ''); ?></textarea>
            </div>

            <div class="form-group">
                <label>অফিসিয়াল টেলিগ্রাম চ্যানেল লিংক (Channel Link)</label>
                <input type="text" name="force_join_url" value="<?php echo htmlspecialchars($config['force_join_url'] ?? 'https://t.me/mmtechnical420'); ?>">
            </div>
        </div>

        <!-- SECTION 3: APP VERSION & UPDATE CONTROL -->
        <div class="card" style="border-left: 4px solid var(--warning-amber);">
            <div class="card-header">
                <div class="card-title">
                    <span class="card-icon">🚀</span>
                    <span>অ্যাপ ভার্সন ও আপডেট (Version & Force Update)</span>
                </div>
                <div class="switch-container">
                    <label class="switch">
                        <input type="checkbox" name="force_update" <?php echo !empty($config['force_update']) ? 'checked' : ''; ?>>
                        <span class="slider"></span>
                    </label>
                </div>
            </div>

            <div class="row-2">
                <div class="form-group">
                    <label>লেটেস্ট ভার্সন নাম (Latest Version Name)</label>
                    <input type="text" name="latest_version" value="<?php echo htmlspecialchars($config['latest_version'] ?? '1.0'); ?>" placeholder="যেমন: 1.0 বা 1.1">
                </div>
                <div class="form-group">
                    <label>মিনিমাম ভার্সন কোড (Min Required Code)</label>
                    <input type="number" name="min_required_version" value="<?php echo intval($config['min_required_version'] ?? 1); ?>" min="1">
                    <div class="input-hint">বর্তমান অ্যাপের ভার্সন কোড হলো 1</div>
                </div>
            </div>

            <div class="form-group">
                <label>আপডেট টাইটেল (Update Title)</label>
                <input type="text" name="update_title" value="<?php echo htmlspecialchars($config['update_title'] ?? 'Update Required'); ?>">
            </div>

            <div class="form-group">
                <label>আপডেট বিবরণ মেসেজ (Update Message)</label>
                <textarea name="update_message" placeholder="নতুন আপডেটে কি কি আছে লিখুন..."><?php echo htmlspecialchars($config['update_message'] ?? ''); ?></textarea>
            </div>

            <div class="form-group">
                <label>ডাউনলোড লিংক (APK Download / Telegram Link)</label>
                <input type="text" name="download_url" value="<?php echo htmlspecialchars($config['download_url'] ?? 'https://t.me/mmtechnical420'); ?>">
            </div>
        </div>

        <!-- SAVE BUTTON -->
        <div class="btn-save-sticky">
            <button type="submit" name="save_settings" class="btn-save">
                💾 সেটিংস সংরক্ষণ করুন (Save Changes)
            </button>
        </div>

    </form>
</div>

</body>
</html>
