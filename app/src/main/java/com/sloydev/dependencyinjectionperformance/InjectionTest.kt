package com.sloydev.dependencyinjectionperformance

import android.os.Build
import android.util.Log
import com.sloydev.dependencyinjectionperformance.custom.DIContainer
import com.sloydev.dependencyinjectionperformance.custom.customJavaModule
import com.sloydev.dependencyinjectionperformance.custom.customKotlinModule
import com.sloydev.dependencyinjectionperformance.dagger2.DaggerJavaDaggerComponent
import com.sloydev.dependencyinjectionperformance.dagger2.DaggerKotlinDaggerComponent
import com.sloydev.dependencyinjectionperformance.dagger2.JavaDaggerComponent
import com.sloydev.dependencyinjectionperformance.dagger2.KotlinDaggerComponent
import com.sloydev.dependencyinjectionperformance.koin.koinJavaModule
import com.sloydev.dependencyinjectionperformance.koin.koinKotlinModule
import org.kodein.di.Kodein
import org.kodein.di.direct
import org.kodein.di.erased.instance
import org.koin.core.KoinApplication
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.logger.EmptyLogger
import org.koin.dsl.koinApplication
import javax.inject.Inject
import kotlin.system.measureNanoTime

class InjectionTest : KoinComponent {

    private val kotlinDaggerTest = KotlinDaggerTest()
    private val javaDaggerTest = JavaDaggerTest()

    private val rounds = 100

    fun runTests() {
        Log.d("KOIN-RESULT", " ")
        Log.d("KOIN-RESULT", "=========|=====================")
        Log.d("KOIN-RESULT", "Device:  | ${Build.BRAND} ${Build.DEVICE} v${Build.VERSION.RELEASE}")
        runKoinKotlinInjection()
        runKoinJavaInjection()
        runKodeinKotlinInjection()
        runKodeinJavaInjection()
        runDaggerKotlinInjection()
        runDaggerJavaInjection()
        runCustomKotlinInjection()
        runCustomJavaInjection()
        Log.d("KOIN-RESULT", "=========|=====================")
        Log.d("KOIN-RESULT", " ")
    }

    private fun runKoinKotlinInjection() {
        lateinit var koin: KoinApplication
        val startup = measureTime {
            koin = koinApplication {
                useLogger(logger = EmptyLogger())
                loadModules(koinKotlinModule)
            }.start()
        }

        val durations = (1..rounds).map {
            measureTime {
                get<Fib8>()
            }
        }
        koin.stop()
        report(durations, startup, "Koin + Kotlin")
    }

    private fun runKoinJavaInjection() {
        lateinit var koin: KoinApplication
        val startup = measureTime {
            koin = koinApplication {
                useLogger(logger = EmptyLogger())
                loadModules(koinJavaModule)
            }.start()
        }

        val durations = (1..rounds).map {
            measureTime {
                get<FibonacciJava.Fib8>()
            }
        }
        koin.stop()
        report(durations, startup, "Koin + Java")
    }

    private fun runKodeinKotlinInjection() {
        lateinit var kodein: Kodein
        val startup = measureTime {
            kodein = Kodein {
                import(kodeinKotlinModule)
            }
        }
        val durations = (1..rounds).map {
            measureTime {
                kodein.direct.instance<Fib8>()
            }
        }
        report(durations, startup, "Kodein + Kotlin")
    }

    private fun runKodeinJavaInjection() {
        lateinit var kodein: Kodein
        val startup = measureTime {
            kodein = Kodein {
                import(kodeinJavaModule)
            }
        }
        val durations = (1..rounds).map {
            measureTime {
                kodein.direct.instance<FibonacciJava.Fib8>()
            }
        }
        report(durations, startup, "Kodein + Java")
    }

    private fun runDaggerKotlinInjection() {
        lateinit var component: KotlinDaggerComponent
        val startup = measureTime {
            component = DaggerKotlinDaggerComponent.create()
        }
        val durations = (1..rounds).map {
            measureTime {
                component.inject(kotlinDaggerTest)
            }
        }
        report(durations, startup, "Dagger2 + Kotlin")
    }

    private fun runDaggerJavaInjection() {
        lateinit var component: JavaDaggerComponent
        val startup = measureTime {
            component = DaggerJavaDaggerComponent.create()
        }
        val durations = (1..rounds).map {
            measureTime {
                component.inject(javaDaggerTest)
            }
        }
        report(durations, startup, "Dagger2 + Java")
    }

    private fun runCustomKotlinInjection() {
        val startup = measureTime {
            DIContainer.loadModule(customKotlinModule)
        }
        val durations = (1..rounds).map {
            measureTime {
                DIContainer.get<Fib8>()
            }
        }
        report(durations, startup, "Custom + Kotlin")
    }

    private fun runCustomJavaInjection() {
        val startup = measureTime {
            DIContainer.loadModule(customJavaModule)
        }
        val durations = (1..rounds).map {
            measureTime {
                DIContainer.get<FibonacciJava.Fib8>()
            }
        }
        report(durations, startup, "Custom + Java")
    }

    private fun report(durations: List<Double>, startup: Double, testName: String) {
        Log.d("KOIN-RESULT", "---------|--------------------")
        Log.d("KOIN-RESULT", "Test:    | $testName")
        Log.d("KOIN-RESULT", "Startup: | ${startup.format()} ms")
        Log.d("KOIN-RESULT", "Min-Max: | ${durations.min().format()}-${durations.max().format()} ms")
        Log.d("KOIN-RESULT", "Average: | ${durations.average().format()} ms")
    }

    private fun Double?.format() = String.format("%.2f", this)

    private fun measureTime(block: () -> Unit) = measureNanoTime(block) / 1000000.0

    class KotlinDaggerTest {
        @Inject
        lateinit var daggerFib8: Fib8
    }

    class JavaDaggerTest {
        @Inject
        lateinit var daggerFib8: FibonacciJava.Fib8
    }
}