package org.timt.PhotoSorter

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class EndPoint {

    @Autowired
    lateinit var main: Main

    @GetMapping
    fun start() {
        main.orchestration("c:\\temp")
    }
}