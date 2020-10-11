package com.seasonnow.data

import com.seasonnow.data.SeasonData.Season.Season

object SeasonData {

  object Season extends Enumeration {
    type Season = Value

    val WINTER = Value("Winter")
    val SPRING = Value("Spring")
    val SUMMER = Value("Summer")
    val FALL = Value("Fall")
    val NOT_FETCHED = Value("Unknown")
  }

  case class SeasonInfo(temp: Double, zipcode: Option[String], season: Season)

}
