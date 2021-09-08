package org.timt.PhotoSorter

import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Month

data class PhotoData(val Path: Path, val fileTime: FileTime, val month: Month, val year: Int) {}
