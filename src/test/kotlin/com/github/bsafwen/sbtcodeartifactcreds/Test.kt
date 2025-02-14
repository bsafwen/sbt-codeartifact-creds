package com.github.bsafwen.sbtcodeartifactcreds

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class Test : BasePlatformTestCase() {

    fun testXMLFile() {
        assertEquals("foo", "foo")
    }

    override fun getTestDataPath() = "src/test/testData/rename"
}
