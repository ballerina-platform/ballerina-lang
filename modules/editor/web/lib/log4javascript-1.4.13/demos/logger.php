<?php
session_start();
$data = urldecode( file_get_contents("php://input") ) . "\r\n";
if (isset($_SESSION['ajax_demo_log'])) {
    $_SESSION['ajax_demo_log'] .= $data;
} else {
    $_SESSION['ajax_demo_log'] = $data;
}
?>