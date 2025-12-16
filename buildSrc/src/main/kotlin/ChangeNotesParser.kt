import java.nio.file.Files
import java.nio.file.Paths

/**
 * 从 CHANGELOG.md 文件中解析变更日志
 */
fun main() {
    val changelogPath = Paths.get("CHANGELOG.md")
    try {
        val changeNotes = parseChangeNotesFromReadme(changelogPath)
        println(changeNotes)
    } catch (e: Exception) {
        println("Error parsing changelog: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * 从 CHANGELOG.md 文件中解析变更日志
 * @return 格式化的 HTML 变更日志
 */
fun parseChangeNotesFromReadme(changelogPath: java.nio.file.Path): String {
    val lines = Files.readAllLines(changelogPath)

    data class VersionNotes(val version: String, val blocks: List<List<String>>)

    val versions = mutableListOf<VersionNotes>()

    var currentVersion: String? = null
    var currentBlock = mutableListOf<String>()
    var allBlocks = mutableListOf<List<String>>()
    var isVersionBlock = false

    fun flushBlock() {
        if (currentBlock.isNotEmpty()) {
            allBlocks.add(currentBlock)
            currentBlock = mutableListOf()
        }
    }

    fun flushVersion() {
        if (currentVersion != null && allBlocks.isNotEmpty()) {
            versions.add(VersionNotes(currentVersion!!, allBlocks.toList()))
        }
        allBlocks = mutableListOf()
    }

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("[//]: # (version-start)")) {
            isVersionBlock = true
        }
        if (!isVersionBlock) {
            continue
        }
        // 检测版本块结束标记
        if (trimmed.startsWith("[//]: # (version-end)")) {
            break
        }
        when {
            trimmed.startsWith("### ") -> {
                flushBlock()
                flushVersion()
                currentVersion = trimmed.removePrefix("### ").trim()
            }

            trimmed.isEmpty() -> {
                flushBlock()
            }

            trimmed.startsWith("- ") && currentVersion != null -> {
                currentBlock.add(trimmed.removePrefix("- ").trim())
            }
        }
    }
    flushBlock()
    flushVersion()

    // 倒序（新版本在最前面）
    versions.reverse()

    val result = StringBuilder()
    for (v in versions) {
        result.append("<h3>${v.version}</h3>\n")
        for (block in v.blocks) {
            result.append("<ul>\n")
            block.forEach { item ->
                result.append("    <li>${item}</li>\n")
            }
            result.append("</ul><br>\n")
        }
    }

    return result.toString()
}
