/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.google.inject.{Inject, Singleton}
import controllers.routes
import play.api.Configuration
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{Call, Request}

import java.net.{URI, URLEncoder}
import java.time.LocalDate

@Singleton
class FrontendAppConfig @Inject() (val configuration: Configuration) {

  private final val ENGLISH = "en"
  private final val WELSH = "cy"

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "trusts"

  val repositoryKey: String = "assets"

  val appName: String = configuration.get[String]("appName")
  val analyticsToken: String = configuration.get[String](s"google-analytics.token")
  val analyticsHost: String = configuration.get[String](s"google-analytics.host")
  val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  val betaFeedbackUrl = s"$contactHost/contact/beta-feedback"
  val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"

  lazy val countdownLength: String = configuration.get[String]("timeout.countdown")
  lazy val timeoutLength: String = configuration.get[String]("timeout.length")

  lazy val authUrl: String = configuration.get[Service]("auth").baseUrl
  lazy val loginUrl: String = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  lazy val registrationProgressUrlTemplate: String = configuration.get[String]("urls.registrationProgress")
  def registrationProgressUrl(draftId: String): String = registrationProgressUrlTemplate.replace(":draftId", draftId)

  lazy val logoutUrl: String = configuration.get[String]("urls.logout")

  lazy val trustsUrl: String = configuration.get[Service]("microservice.services.trusts").baseUrl

  lazy val maintainATrustFrontendUrl : String = configuration.get[String]("urls.maintainATrust")
  lazy val createAgentServicesAccountUrl : String = configuration.get[String]("urls.createAgentServicesAccount")

  lazy val locationCanonicalList: String = configuration.get[String]("location.canonical.list.all")
  lazy val locationCanonicalListNonUK: String = configuration.get[String]("location.canonical.list.nonUK")

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")

  private val day: Int = configuration.get[Int]("minimumDate.day")
  private val month: Int = configuration.get[Int]("minimumDate.month")
  private val year: Int = configuration.get[Int]("minimumDate.year")
  lazy val minDate: LocalDate = LocalDate.of(year, month, day)

  lazy val assetValueUpperLimitExclusive: Long = configuration.get[Long]("assetValueUpperLimitExclusive")
  lazy val assetValueLowerLimitExclusive: Long = configuration.get[Long]("assetValueLowerLimitExclusive")

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang(ENGLISH),
    "cymraeg" -> Lang(WELSH)
  )

  def routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  def accessibilityLinkUrl(implicit request: Request[_]): String = {
    val userAction = URLEncoder.encode(new URI(request.uri).getPath, "UTF-8")
    lazy val accessibilityBaseLinkUrl: String = configuration.get[String]("urls.accessibility")
    s"$accessibilityBaseLinkUrl?userAction=$userAction"
  }

  def helplineUrl(implicit messages: Messages): String = {
    val path = messages.lang.code match {
      case WELSH => "urls.welshHelpline"
      case _ => "urls.trustsHelpline"
    }

    configuration.get[String](path)
  }
}
