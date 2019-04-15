import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {requireNativeComponent, View, ViewPropTypes} from 'react-native';

type PropsType = {
    minValue?: number;
    maxValue?: number;
    defaultValue?: number;
    step?: number;
    num?: number;
    unit?: string;
    resultNumTextSize:? number;
    unitTextSize:? number;
    resultNumTextColor:? string;
    unitColor:? string;
} & typeof
(View);

export default class ReactScrollRuler extends Component {
    static propTypes = {
        minValue: PropTypes.number.isRequired,
        maxValue: PropTypes.number.isRequired,
        defaultValue: PropTypes.number.isRequired,
        step: PropTypes.number.isRequired,
        num: PropTypes.number.isRequired,
        unit: PropTypes.string,
        resultNumTextSize: PropTypes.number,
        unitTextSize: PropTypes.number,
        resultNumTextColor: PropTypes.string,
        unitColor: PropTypes.string,
        onSelect: PropTypes.func,
        ...ViewPropTypes,
    };
    props: PropsType;
    rulerRef: any;

    setNativeProps(props: PropsType) {
        this.rulerRef.setNativeProps(props);
    }

    _onSelect = (event) => {
        if (!this.props.onSelect) {
            return;
        }
        this.props.onSelect(event.nativeEvent.value);
    }

    render() {
        const {
            minValue,
            maxValue,
            defaultValue,
            step,
            num,
            unit,
            onSelect,
            resultNumTextSize,
            unitTextSize,
            resultNumTextColor,
            unitColor,
            textStyle,
            unitLabelStyle,
            ...otherProps
        } = this.props;

        return (
            <RNScrollRuler
                ref={(component) => {
                    this.rulerRef = component;
                }}
                minValue={minValue}
                maxValue={maxValue}
                defaultValue={defaultValue}
                step={step}
                num={num}
                unit={unit}
                resultNumTextSize={resultNumTextSize}
                unitTextSize={unitTextSize}
                resultNumTextColor={resultNumTextColor}
                unitColor={unitColor}
                onSelect={this._onSelect}
                {...otherProps}
            />
        );
    }
}

const RNScrollRuler = requireNativeComponent('RNScrollRuler', ReactScrollRuler, {
    nativeOnly: {onSelect: true}
});