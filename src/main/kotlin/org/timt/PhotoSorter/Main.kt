package org.timt.PhotoSorter

import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.time.ZoneId
import java.util.*
import java.util.logging.Logger
import java.util.stream.Collectors
import kotlin.io.path.isRegularFile

@Component
class Main {
    companion object {
        val logger = Logger.getLogger(Main::class.java.name)
    }

    val photoList = arrayListOf<PhotoData>()

    fun orchestration(path: String) {
        getAllFilesRecursion(Paths.get(path))
        buildDir(path)
        moveAllFiles(path)
    }
    /**
     * Чтение стуктуры файлов папкок начиная с pathStr с помощью Files.walk
     * @param String
     * @return List<Path>
     */
    private fun getAllFilePath(pathStr: String): List<Path> {
        var files = emptyList<Path>()
        try {
            files = Files.walk(Paths.get(pathStr)).filter { path: Path? -> Files.isRegularFile(path)
            }.collect(Collectors.toList())
            logger.info("Стуктура файлов прочитана. Всего " + files.size)
        } catch (e: IOException) {
            logger.severe("ошибка чтения структуры файлов")
        }
        return files
    }

    /**
    * Заполнение PhotoData, получение времени создания файла
     * @param filesList
     * @return List<PhotoData>
     */
    private fun getFileCreationDate(filesList: List<Path>): List<PhotoData> {
        var photoDataList = arrayListOf<PhotoData>()
        filesList.stream().forEach { f: Path ->
            try {
                val fileAttr = Files.readAttributes(f.toAbsolutePath(), BasicFileAttributes::class.java)
                val localDate = Date(fileAttr.creationTime().toMillis()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                photoDataList.add(PhotoData(f.toAbsolutePath(), fileAttr.creationTime(), localDate.month, localDate.year))
            } catch (e: IOException) {
                logger.severe("Ошибка чтения аттрибутов файла")
            }
        }
        logger.info("Даты создания файлов получены")
        return photoDataList
    }

    /**
     * Рекурсивное чтение файлов и заполнение PhotoData
     * @param Path
     */
    private fun getAllFilesRecursion(path: Path) {
        val sDirList = Files.list(path)
        sDirList.forEach {
            if (it.isRegularFile()) {
                val fileAttr = Files.readAttributes(it.toAbsolutePath(), BasicFileAttributes::class.java)
                val localDate = Date(fileAttr.creationTime().toMillis()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                photoList.add(PhotoData(it.toAbsolutePath(), fileAttr.creationTime(), localDate.month, localDate.year))
            } else {
                getAllFilesRecursion(it)
            }
        }
    }

    /**
     * Создание структуры каталогов вида Год месяц
     * @param startDir: String
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun buildDir(startDir: String)  {
        val resultDir = startDir + File.separator + "result"
        val dirNames = hashSetOf<String>()
        photoList.forEach {
            dirNames.add(it.year.toString() + " " + it.month)
        }
        try {
            Files.createDirectories(Paths.get(resultDir))
            dirNames.forEach {
                Files.createDirectories(Paths.get(resultDir + File.separator + it))
            }
        } catch (e: IOException) {
            logger.severe("Папки не созданы")
            logger.severe(e.message)
            throw e
        }
        logger.info("Папки созданы")
    }

    /**
     * Перемещение файлов в папки в соответсвии с датой создания
     * @param targetDir: String
     */
    private fun moveAllFiles(targetDir: String) {
        photoList.forEach {
            val targetPath = Paths.get(targetDir + File.separator + it.year + " " + it.month + File.separator + it.Path.fileName)
            Files.move(it.Path, targetPath, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}