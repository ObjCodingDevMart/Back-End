package org.example.devmarketbackend.s3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.ObjectMetadata
import org.example.devmarketbackend.dto.response.S3Item
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.util.UUID

@Service
class S3Service(
    private val amazonS3: AmazonS3,
    private val s3Properties: S3Properties
) {
    fun uploadFile(file: MultipartFile): String {
        val bucketName = s3Properties.bucket
        val fileName = "${UUID.randomUUID()}_${file.originalFilename}"
        try {
            val metadata = ObjectMetadata()
            metadata.contentType = file.contentType
            metadata.contentLength = file.size
            amazonS3.putObject(bucketName, fileName, file.inputStream, metadata)
            return amazonS3.getUrl(bucketName, fileName).toString()
        } catch (e: IOException) {
            throw RuntimeException("s3 upload failed", e)
        } catch (e: AmazonS3Exception) {
            throw RuntimeException("s3 upload failed", e)
        }
    }

    fun deleteFile(fileName: String) {
        val bucketName = s3Properties.bucket
        try {
            amazonS3.deleteObject(bucketName, fileName)
        } catch (e: AmazonS3Exception) {
            throw RuntimeException("s3 delete failed", e)
        }
    }

    fun getUrl(fileName: String): String {
        val bucketName = s3Properties.bucket
        try {
            return amazonS3.getUrl(bucketName, fileName).toString()
        } catch (e: AmazonS3Exception) {
            throw RuntimeException("s3 url failed", e)
        }
    }

    fun uploadFileForItem(file: MultipartFile): S3Item {
        val bucketName = s3Properties.bucket
        val fileName = "${UUID.randomUUID()}_${file.originalFilename}"
        try {
            val metadata = ObjectMetadata()
            metadata.contentType = file.contentType
            metadata.contentLength = file.size
            amazonS3.putObject(bucketName, fileName, file.inputStream, metadata)
            val url = amazonS3.getUrl(bucketName, fileName).toString()
            return S3Item(fileName, url)
        } catch (e: IOException) {
            throw RuntimeException("s3 upload failed", e)
        } catch (e: AmazonS3Exception) {
            throw RuntimeException("s3 upload failed", e)
        }
    }
}

