package simulation

import probability_monad._

object Data {
  val updateSize = 1500
  val updateIntervalSeconds = 10
  val frontpageSize = 90
  val newPageSize = 90
  val newFrontPageVotingRatio = 0.1 // TODO
  val minimVotesForFrontpage = 3 //TODO

  val nextSubmissionArrivalDelay = {
    val averageSubmissionArrivalSeconds = 78.290865 // from bigquery 2021
    Distribution.exponential(
      1.0 / averageSubmissionArrivalSeconds
    )
  }

  val nextVoteArrivalDelay = {
    val averageVoteArrivalSeconds =
      1.0 / (6106438.0 / 365 / 24 / 3600) // ~5 from bigquery
    Distribution.exponential(
      1.0 / averageVoteArrivalSeconds
    )
  }

  // select sum(gain) as rankgain from dataset where samplingWindow >= 3 and newRank is not null and topRank is null and showRank = -1 and askRank = -1 group by newRank order by newRank;
  val voteGainOnNewRankDistribution = Distribution.discrete(
    Array[Double](
      1464, 2017, 2162, 1948, 1887, 1784, 1635, 1535, 1445, 1353, 1296, 1171,
      1099, 1069, 1028, 973, 947, 886, 925, 854, 837, 787, 807, 747, 655, 627,
      592, 577, 624, 546, 495, 467, 407, 354, 362, 387, 385, 333, 336, 296, 266,
      321, 293, 259, 253, 254, 273, 233, 260, 246, 228, 206, 233, 187, 193, 235,
      208, 218, 179, 234, 197, 187, 170, 171, 183, 178, 193, 164, 149, 161, 166,
      145, 141, 163, 157, 140, 132, 144, 141, 134, 149, 137, 133, 164, 116, 153,
      157, 133, 154, 147
    ).zipWithIndex.map { case (d, i) => (i, d) }: _*
  )

  // select sum(gain) as rankgain from dataset where samplingWindow >= 3 and topRank is not null group by topRank order by topRank;
  val voteGainOnTopRankDistribution = Distribution.discrete(
    Array[Double](
      100481, 57457, 44419, 37889, 33027, 28484, 25084, 23950, 22680, 20959,
      19343, 17739, 17320, 15863, 15862, 15325, 15109, 14478, 14006, 13215,
      12963, 12159, 11869, 11447, 11249, 10979, 10970, 10640, 10569, 10370,
      6871, 5278, 4855, 4199, 4232, 4028, 3920, 3646, 3411, 3573, 3251, 3242,
      3240, 3027, 3129, 2913, 2611, 2616, 2521, 2548, 2408, 2452, 2271, 2190,
      2190, 2128, 2050, 2061, 1946, 1983, 1824, 1592, 1375, 1319, 1275, 1228,
      1247, 1200, 1160, 1151, 1085, 1115, 1022, 1022, 977, 949, 929, 933, 916,
      961, 867, 910, 830, 859, 801, 843, 768, 863, 827, 971
    ).zipWithIndex.map { case (d, i) => (i, d) }: _*
  )
}
