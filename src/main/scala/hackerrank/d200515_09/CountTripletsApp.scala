package hackerrank.d200515_09

/**
  * https://www.hackerrank.com/challenges/count-triplets-1/problem?h_l=interview&playlist_slugs%5B%5D=interview-preparation-kit&playlist_slugs%5B%5D=dictionaries-hashmaps
  * 8 of 13 Your code did not execute within the time limits
  */
object CountTripletsApp extends App {

  def countTriplets(a: Array[Long], r: Long): Long = {
    val len = a.length
    (0 to len-3)
      .map { i => (i, a(i)*r) }
      .flatMap { case (i, x) =>
        (i+1 to len-2)
          .filter { j => a(j) == x }
          .map { j => (j, a(j)*r) }
          .flatMap { case (j, y) =>
            (j+1 to len-1)
              .filter { k => a(k) == y }
              .map { k =>
                (i,j,k)
              }
          }
      }
      .length
  }
  // exp: 161700
  // fact: 171598
  println(countTriplets(Array.fill[Long](100)(1),1))
}
