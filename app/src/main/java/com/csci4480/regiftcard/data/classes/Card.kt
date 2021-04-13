package com.csci4480.regiftcard.data.classes

class Card(var company_name: String = "", var card_num: String = "", var card_worth: Int = 0, var companies_accepted: MutableList<String> = mutableListOf("")) {
}