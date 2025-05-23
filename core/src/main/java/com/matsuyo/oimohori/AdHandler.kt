package com.matsuyo.oimohori

interface AdHandler {
    fun showAdDialog(message: String, onRewardGranted: Runnable)
    fun showMessageDialog(message: String)
}
