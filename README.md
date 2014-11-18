**Proper Functional IO with Play Iteratees**
 
The current IO implementation in Play Iteratees is not functional enough, it is using mutable state over OutputStream 
or InputStream see [EnumeratorsSpec](https://github.com/playframework/playframework/blob/master/framework/src/iteratees/src/test/scala/play/api/libs/iteratee/EnumeratorsSpec.scala)

The suggested approach in this repo uses [AsynchronousFileChannel](https://docs.oracle.com/javase/8/docs/api/java/nio/channels/AsynchronousFileChannel.html)
to handle asynchronous read/writes over a file and wraps them functional structures Reader/Writer that always return
 a new object on every apply, they also wrap scala.concurrent.Future making them very easy to integrate with Play Iteratees
 
 
 Reader is an enumerator over a file
```scala
...
val enumerator: Enumerator[Array[Byte]] = Readers.toEnumerator("file", 5)
val eventualArray = enumerator |>>> Iteratee.fold(Array[Byte]())((chunk, result) => chunk ++ result)
result(eventualArray, 5 seconds) //file content should be here
...
```

Writer is an Iteratee over that writes to a file the byte arrays pushed by an Enumerator
```scala
...
val iterateeWriter = Writers.toIteratee("file")
val eventualWriter = input |>>> iterateeWriter
result(eventualWriter, 5 seconds) // the number of bytes written
```

For more detail see ReaderSpec and WriterSpec
 
 
 
