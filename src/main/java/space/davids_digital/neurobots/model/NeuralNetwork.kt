package space.davids_digital.neurobots.model;

public class NeuralNetwork {
    private final double[][][] weights;
    private final int hiddenLayerNeuronsN;
    private final int hiddenLayersN;
    private final int inputsN;
    private final int outputsN;

    public NeuralNetwork(int inputsN, int hiddenLayersN, int hiddenLayerNeuronsN, int outputsN, double minWeight, double maxWeight) {
        this.hiddenLayerNeuronsN = hiddenLayerNeuronsN;
        this.hiddenLayersN = hiddenLayersN;
        this.inputsN = inputsN;
        this.outputsN = outputsN;
        weights = new double[hiddenLayersN + 1][][];
        for (int layerId = 0; layerId < hiddenLayersN + 1; layerId++) {
            int layerNeuronsN = layerId == hiddenLayersN ? outputsN : hiddenLayerNeuronsN;

            weights[layerId] = new double[layerNeuronsN][];
            for (int neuronTo = 0; neuronTo < layerNeuronsN; neuronTo++) {

                int neuronsFrom = layerId == 0 ? inputsN : hiddenLayerNeuronsN;
                weights[layerId][neuronTo] = new double[neuronsFrom];

                for (int neuronFrom = 0; neuronFrom < neuronsFrom; neuronFrom++)
                    weights[layerId][neuronTo][neuronFrom] = minWeight + Math.random()*(maxWeight - minWeight);
            }
        }
    }

    public void mutate(double maxMutation) {
        for (int layerId = 0; layerId < weights.length; layerId++) {
            for (int neuronTo = 0; neuronTo < weights[layerId].length; neuronTo++) {
                for (int neuronFrom = 0; neuronFrom < weights[layerId][neuronTo].length; neuronFrom++) {
                    weights[layerId][neuronTo][neuronFrom] += maxMutation*(1 - 2*Math.random());
                }
            }
        }
    }

    public double[] getResponse(double[] input) {
        double[] prevSignals = input;
        for (int layerId = 0; layerId < hiddenLayersN + 1; layerId++) {
            int neuronsN = layerId == hiddenLayersN ? outputsN : hiddenLayerNeuronsN;
            double[] resultSignal = new double[neuronsN];
            for (int neuronTo = 0; neuronTo < neuronsN; neuronTo++) {
                double sum = 0;
                for (int neuronFrom = 0; neuronFrom < (layerId == 0 ? inputsN : prevSignals.length); neuronFrom++)
                    sum += weights[layerId][neuronTo][neuronFrom] * prevSignals[neuronFrom];
                resultSignal[neuronTo] = activate(sum);
            }
            prevSignals = resultSignal;
        }
        return prevSignals;
    }

    private double activate(double value) {
        return Math.tanh(value);
    }
}
