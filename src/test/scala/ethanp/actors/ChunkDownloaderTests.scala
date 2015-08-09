package ethanp.actors

import java.io.{File, FileInputStream}

import akka.actor.Props
import akka.testkit.TestActorRef
import ethanp.file.LocalP2PFile
import ethanp.firstVersion._


/**
 * Created by Ethan Petuchowski on 7/2/15.
 *
 */
class BaseChunkDLTester extends BaseTester {

    /* this is where the ChunkDownloader will store the Chunks it receives */
    val localOutFile = new File("testfiles/output1.txt")
    localOutFile.delete()
    localOutFile.deleteOnExit()

    /* this file has 3 chunks */
    val p2pF = LocalP2PFile.empty(inputTextP2P.fileInfo, localOutFile)

    /* our ChunkDownloader-under-test is only responsible for the first chunk
     * there are 3 pieces in this chunk
     */
    val chunkIdx = 0

    val chunkSize = inputTextP2P.fileInfo numBytesInChunk chunkIdx

    /* create a ChunkDownloader who shall
     * request chunkIdx = 0
     * of file = p2pF
     * from peer = self (the test script)
     */
    val cDlRef = TestActorRef(Props(classOf[ChunkDownloader], p2pF, chunkIdx, self))
    val cDlPtr: ChunkDownloader = cDlRef.underlyingActor
    quickly(expectMsgClass(classOf[ChunkRequest]))
    cDlRef ! AddMeAsListener
}

class ChunkDLValidDataTest extends BaseChunkDLTester {
    "this test" should {
        "not already have data in the outfile" in {
            if (localOutFile.exists()) {
                localOutFile should have length 0
            }
        }
    }
    "receiving valid pieces" should {
        "have the right receiver buffer" in {
            cDlPtr.piecesRcvd should have size 3
        }
        "mark first piece received (and only it) off" in {
            val bytes = inputTextP2P.getPiece(chunkIdx, 0).get
            cDlRef ! Piece(bytes, 0)
            quickly {
                cDlPtr.piecesRcvd shouldEqual Array(true, false, false)
            }
        }
        "mark rest of pieces off" in {
            /* send the rest of the pieces over */
            for (i <- 1 until cDlPtr.piecesRcvd.length)
                cDlRef ! Piece(inputTextP2P.getPiece(chunkIdx, i).get, i)
            quickly {
                cDlPtr.piecesRcvd shouldEqual Array(true, true, true)
            }
        }
        "notify listeners of download success" in {
            // still not sure this piece of the protocol will ever come in handy
            // (btw, the Client already KNOWS the chunk size from `FileInfo` object)
            cDlRef ! ChunkSuccess
            quickly {
                expectMsg(ChunkComplete(chunkIdx))
            }
        }
        "write chunk of CORRECT data to disk" in {
            localOutFile should exist

            val realFileReader = new FileInputStream(inputTextP2P.file)
            val fileContentChecker = new FileInputStream(localOutFile)

            val realData = new Array[Byte](chunkSize)
            val writtenData = new Array[Byte](chunkSize)

            fileContentChecker read writtenData
            realFileReader read realData

            writtenData shouldEqual realData
        }
    }
}
class ChunkDLInvalidDataTest extends BaseChunkDLTester {
    "receiving invalid data" should {
        "not write the chunk to disk" in {
            // TODO
        }
        "notify parent of bad peer" in {
            // TODO
        }
    }
}
