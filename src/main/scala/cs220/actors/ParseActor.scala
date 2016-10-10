package cs220.actors

import akka.actor.Actor
import akka.actor.{ActorRef, ActorLogging}

/**
 * The `ParseActor` handles the parsing of HTML documents. Its
 * primary goal is to extract out the links and words contained
 * in an HTML document.
 */
class ParseActor(pq: ActorRef) extends Actor with ActorLogging {
  log.info("ParseActor created")
  pq ! NeedPage
  val headRegex = """(?s)<head.*>.*</head>""".r
  val scriptRegex = """(?s)<script.*>.*</script>""".r
  val styleRegex = """(?s)<style.*>.*</style>""".r
  val commentRegx = """(?s)<!--.*-->""".r
  val linkRgex = """(https?://[^\"]+)""".r
  val whiteSpaceRegex = """[ \t\x0B\f\r]+""".r
  val tagRegex = """</?[^>]+>""".r
  val nonwordRegex = """\W""".r

  def receive = {
    // TODO
    case ParsePage(url,html)    =>{
                                  var lines = html
                                  /* replace the header of the HTML file into
                                  a empty string*/
                                  lines = headRegex replaceAllIn(lines,"")
                                  /* replace the script of the HTML file into
                                  a empty string */
		                              lines = scriptRegex replaceAllIn(lines,"")
                                  /*replace all style tag of the HTML file into
                                  a empty string*/
		                              lines = styleRegex replaceAllIn(lines,"")
                                  /*replace all comment tag of the HTML file into
                                  a empty string*/
		                              lines = commentRegx replaceAllIn(lines,"")
                                  /*take all links out the HTML file*/
		                              val links = (linkRgex findAllIn lines).mkString(",")
		                              val arrayLinks = links.split(",")
                                  //replace all other tag with empty string
                                  lines = linkRgex replaceAllIn(lines,"")
                                  lines = tagRegex replaceAllIn(lines,"")
                                  //replace nonWord character with a space
                                  lines = nonwordRegex replaceAllIn(lines," ")
                                  //replace multiple with a single whiteSpace
                                  lines = whiteSpaceRegex replaceAllIn(lines," ")
		                              /*split the modify HTML file by whiteSpaces into
                                  a list, and drop the head if they more than one
                                  character*/
                                  val k = lines.length match {
			                                   case 1 => lines.split(" ")
						                             case 0 => lines.split(" ")
			                                   case _	=> lines.split(" ").tail
					                                  }
                                  //send each link back to the sender
                                  arrayLinks.foreach(e => sender ! Link(e))
                                  //send each word back to the sender
                                  k.foreach(e => sender ! Word(url,e))
                                  // request an other link
                                  pq ! NeedPage
                                }

    case NoPages                => sender ! NeedPage

  }
}
