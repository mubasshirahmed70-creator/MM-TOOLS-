<?php
/**
 * Dynamic JSON Config Endpoint with ZERO caching
 */
header("Content-Type: application/json; charset=UTF-8");
header("Cache-Control: no-store, no-cache, must-revalidate, max-age=0");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");
header("Expires: 0");
header("Access-Control-Allow-Origin: *");

$configFile = __DIR__ . '/app-config.json';
if (file_exists($configFile)) {
    echo file_get_contents($configFile);
} else {
    echo json_encode([
        'min_required_version' => 1,
        'latest_version' => '1.0',
        'force_update' => false,
        'is_app_disabled' => false,
        'disabled_title' => 'Maintenance Mode',
        'disabled_message' => 'MM Tools is temporarily disabled for maintenance.',
        'update_title' => 'Update Required',
        'update_message' => 'A new version of MM Tools is available.',
        'download_url' => 'https://t.me/mmtechnical420',
        'telegram_url' => 'https://t.me/mmtechnical420',
        'force_join_telegram' => false,
        'force_join_title' => 'Join Telegram Channel',
        'force_join_message' => 'Join our official Telegram channel for updates and announcements.',
        'force_join_url' => 'https://t.me/mmtechnical420',
        'force_join_dismissible' => true
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
}
