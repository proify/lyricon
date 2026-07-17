/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.lyric.style

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class BasicStyleVisibilityRulesTest {

    @Test
    fun missingVisibilityPreferencesHideClockWhilePlayingByDefault() {
        val style = BasicStyle().apply {
            load(preferences())
        }

        assertTrue(
            style.visibilityRules.any {
                it.id == BasicStyle.CLOCK_VIEW_ID &&
                        it.mode == VisibilityRule.MODE_HIDE_WHEN_PLAYING
            }
        )
    }

    @Test
    fun configuredRulesAreMergedWithClockCollisionDefault() {
        val style = BasicStyle().apply {
            load(
                preferences(
                    mapOf(
                        BasicStyle.PREF_KEY_VISIBILITY_RULES to
                                """[{"id":"notification_icon_area","mode":1}]"""
                    )
                )
            )
        }

        assertTrue(
            style.visibilityRules.any {
                it.id == BasicStyle.CLOCK_VIEW_ID &&
                        it.mode == VisibilityRule.MODE_HIDE_WHEN_PLAYING
            }
        )
        assertTrue(
            style.visibilityRules.any {
                it.id == "notification_icon_area" &&
                        it.mode == VisibilityRule.MODE_HIDE_WHEN_PLAYING
            }
        )
    }

    @Test
    fun explicitNormalClockRuleOverridesCollisionDefault() {
        val style = BasicStyle().apply {
            load(
                preferences(
                    mapOf(
                        BasicStyle.PREF_KEY_VISIBILITY_RULES to
                                """[{"id":"clock","mode":0}]"""
                    )
                )
            )
        }

        val clockRules = style.visibilityRules.filter { it.id == BasicStyle.CLOCK_VIEW_ID }
        assertEquals(1, clockRules.size)
        assertEquals(VisibilityRule.MODE_NORMAL, clockRules.single().mode)
    }

    @Test
    fun explicitNormalClockRuleIsKeptForStorage() {
        val rules = BasicStyle.compactVisibilityRulesForStorage(
            listOf(
                VisibilityRule(
                    id = BasicStyle.CLOCK_VIEW_ID,
                    mode = VisibilityRule.MODE_NORMAL
                )
            )
        )

        assertEquals(1, rules.size)
        assertEquals(BasicStyle.CLOCK_VIEW_ID, rules.single().id)
        assertEquals(VisibilityRule.MODE_NORMAL, rules.single().mode)
    }

    @Test
    fun defaultClockRuleIsRemovedFromStorage() {
        val rules = BasicStyle.compactVisibilityRulesForStorage(
            listOf(
                VisibilityRule(
                    id = BasicStyle.CLOCK_VIEW_ID,
                    mode = VisibilityRule.MODE_HIDE_WHEN_PLAYING
                )
            )
        )

        assertTrue(rules.isEmpty())
    }

    @Test
    fun redundantNormalRuleIsRemovedFromStorage() {
        val rules = BasicStyle.compactVisibilityRulesForStorage(
            listOf(
                VisibilityRule(
                    id = "notification_icon_area",
                    mode = VisibilityRule.MODE_NORMAL
                )
            )
        )

        assertTrue(rules.isEmpty())
    }

    private fun preferences(values: Map<String, Any?> = emptyMap()): SharedPreferences {
        return Proxy.newProxyInstance(
            SharedPreferences::class.java.classLoader,
            arrayOf(SharedPreferences::class.java)
        ) { _, method, args ->
            when (method.name) {
                "getAll" -> values
                "contains" -> values.containsKey(args?.get(0))
                "getString", "getStringSet", "getInt", "getLong", "getFloat", "getBoolean" ->
                    values[args?.get(0)] ?: args?.get(1)

                else -> throw UnsupportedOperationException(method.name)
            }
        } as SharedPreferences
    }
}
