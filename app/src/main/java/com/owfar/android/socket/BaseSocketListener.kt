package com.owfar.android.socket

import io.socket.emitter.Emitter

abstract class BaseSocketListener<out Callback>(val callback: Callback) : Emitter.Listener