package me.zombii.horizon.threading;

public class RunnableTicker implements Runnable {

    float fixedUpdateAccumulator = 0.0F;
    double lastUpdateTime = -1.0;
    double lastThreadLoopTime = -1.0;
    double lastAccumulatorCheckTime = -1.0;

    TickingRunnable onTick;

    public RunnableTicker(TickingRunnable onTick) {
        this.onTick = onTick;
    }

    @Override
    public void run() {
        runTicks();
        this.lastThreadLoopTime = this.lastUpdateTime;
    }

    public void runTicks() {
        float fixedUpdateTimestep = 0.05F;
        double curUpdateTime = (double)System.currentTimeMillis();
        if (this.lastAccumulatorCheckTime != -1.0) {
            this.fixedUpdateAccumulator = (float)((double)this.fixedUpdateAccumulator + (curUpdateTime - this.lastAccumulatorCheckTime) / 1000.0);
        }

        for(this.lastAccumulatorCheckTime = curUpdateTime; this.lastUpdateTime == -1.0 || this.fixedUpdateAccumulator >= fixedUpdateTimestep; this.lastUpdateTime = curUpdateTime) {
            onTick.run(fixedUpdateTimestep);
            this.fixedUpdateAccumulator -= fixedUpdateTimestep;
        }

        try {
            float timeLeftUntilNextTick = 0.05F - this.fixedUpdateAccumulator;
            if (timeLeftUntilNextTick > 1.0F) {
                Thread.sleep((long)(timeLeftUntilNextTick * 1000.0F));
            }
        } catch (InterruptedException ignored) {}
    }

}
