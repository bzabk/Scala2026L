package SudokuGame.common

import java.net.http.HttpClient

object HttpClientProvider:
  val client: HttpClient = HttpClient.newHttpClient()