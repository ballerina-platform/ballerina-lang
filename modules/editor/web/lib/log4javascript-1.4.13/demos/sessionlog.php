<?php
session_start();

if (isset($_SESSION['ajax_demo_log'])) {
    echo $_SESSION['ajax_demo_log'];
}
?>