package com.bytes32.rxfs.core

import java.io.File

trait HasTempFile {
  def doWithTempFile(fileName:String)(doWithFile :File => Unit): Unit = {
    val tempFile = File.createTempFile(fileName, "tmp")
    try{
      doWithFile(tempFile)
    } finally {
      tempFile.delete()
    }
  }
}
