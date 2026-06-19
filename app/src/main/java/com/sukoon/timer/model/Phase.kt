package com.sukoon.timer.model

/** The phases a running session moves through. */
enum class Phase {
    Idle,      // nothing running
    GetReady,  // optional lead-in before the first round
    Work,      // a timed round
    Rest,      // interval between rounds
    Done,      // session complete
}
